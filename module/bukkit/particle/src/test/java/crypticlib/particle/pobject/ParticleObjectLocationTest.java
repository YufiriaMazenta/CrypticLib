package crypticlib.particle.pobject;

import crypticlib.particle.utils.matrix.Matrix;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.World;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParticleObjectLocationTest {

    private static final Color TEST_COLOR = Color.fromRGB(35, 105, 210);
    private static Field serverField;
    private static Object previousServer;

    private final List<Emission> emissions = new ArrayList<>();
    private World world;

    @BeforeAll
    static void installVersionOnlyServer() throws ReflectiveOperationException {
        // MinecraftVersion reads Bukkit's version once; no server implementation is needed.
        serverField = Bukkit.class.getDeclaredField("server");
        serverField.setAccessible(true);
        previousServer = serverField.get(null);
        Server server = proxy(Server.class, (instance, method, arguments) -> {
            if (method.getName().equals("getBukkitVersion")) {
                return "1.20.4-R0.1-SNAPSHOT";
            }
            throw new UnsupportedOperationException(method.toString());
        });
        serverField.set(null, server);
    }

    @AfterAll
    static void restoreServer() throws IllegalAccessException {
        if (serverField != null) {
            serverField.set(null, previousServer);
        }
    }

    @BeforeEach
    void recordWorldParticleCalls() {
        world = proxy(World.class, (instance, method, arguments) -> {
            if (method.getName().equals("spawnParticle")) {
                // Record only the API boundary, not a replacement of the rendering algorithm.
                if (arguments[1] instanceof Location) {
                    emissions.add(new Emission((Particle) arguments[0],
                        ((Location) arguments[1]).clone(), (Integer) arguments[2],
                        (Double) arguments[3], (Double) arguments[4],
                        (Double) arguments[5], (Double) arguments[6], arguments[7]));
                } else {
                    emissions.add(new Emission((Particle) arguments[0],
                        new Location((World) instance, (Double) arguments[1],
                            (Double) arguments[2], (Double) arguments[3]),
                        (Integer) arguments[4], (Double) arguments[5],
                        (Double) arguments[6], (Double) arguments[7],
                        (Double) arguments[8], arguments[9]));
                }
                return null;
            }
            throw new UnsupportedOperationException(method.toString());
        });
    }

    static Stream<Arguments> renderingPathsAndIncrements() {
        double[][] increments = {
            {0, 0, 0}, {1.25, 0, 0}, {0, 2, 0}, {0, 0, -3.5}, {1.25, 2, -3.5}
        };
        return Arrays.stream(RenderingPath.values()).flatMap(path ->
            Arrays.stream(increments).map(delta -> Arguments.of(path, delta[0], delta[1], delta[2])));
    }

    @ParameterizedTest(name = "{0}: increment ({1}, {2}, {3})")
    @MethodSource("renderingPathsAndIncrements")
    void renderingDoesNotMutateInputOrAccumulateIncrements(RenderingPath path,
                                                           double x, double y, double z) {
        ParticleProbe object = new ParticleProbe();
        object.setIncrementX(x).setIncrementY(y).setIncrementZ(z);
        Location input = new Location(world, 10, 64, -5, 37, -12);
        Location before = input.clone();

        render(path, object, input);
        render(path, object, input);

        assertAll(
            () -> assertEquals(before, input, "rendering must not mutate the caller's Location"),
            () -> assertEquals(2, emissions.size()),
            () -> assertEmission(emissions.get(0), path, 10 + x, 64 + y, -5 + z),
            () -> assertEmission(emissions.get(1), path, 10 + x, 64 + y, -5 + z));
    }

    @ParameterizedTest
    @EnumSource(RenderingPath.class)
    void matrixTransformsRelativeToOriginBeforeAddingIncrements(RenderingPath path) {
        ParticleProbe object = new ParticleProbe();
        Location origin = new Location(world, 10, 64, -5, 20, 30);
        Location originBefore = origin.clone();
        object.setOriginLocation(origin);
        object.setMatrix(new Matrix(new double[][] {{2, 0, 0}, {0, 3, 0}, {0, 0, -1}}));
        object.setIncrementX(1).setIncrementY(2).setIncrementZ(3);
        Location input = new Location(world, 12, 67, -1, 37, -12);
        Location before = input.clone();

        render(path, object, input);
        render(path, object, input);

        // Relative (2, 3, 4) becomes (4, 9, -4), then origin and increment are added.
        assertAll(
            () -> assertEquals(before, input),
            () -> assertEquals(originBefore, origin, "matrix rendering must not mutate the origin"),
            () -> assertEmission(emissions.get(0), path, 15, 75, -6),
            () -> assertEmission(emissions.get(1), path, 15, 75, -6));
    }

    @ParameterizedTest
    @EnumSource(RenderingPath.class)
    void matrixWithoutOriginKeepsInputUnchangedAndOnlyAddsIncrements(RenderingPath path) {
        ParticleProbe object = new ParticleProbe();
        object.setMatrix(new Matrix(new double[][] {{2, 0, 0}, {0, 3, 0}, {0, 0, -1}}));
        object.setIncrementX(1).setIncrementY(2).setIncrementZ(3);
        Location input = new Location(world, 10, 64, -5, 37, -12);
        Location before = input.clone();

        render(path, object, input);
        render(path, object, input);

        assertAll(
            () -> assertEquals(before, input),
            () -> assertEmission(emissions.get(0), path, 11, 66, -2),
            () -> assertEmission(emissions.get(1), path, 11, 66, -2));
    }

    @Test
    void fullSpawnOverloadPreservesInputAndForwardsExistingParticleParameters() {
        ParticleProbe object = new ParticleProbe();
        object.setIncrementX(1).setIncrementY(2).setIncrementZ(3);
        Location input = new Location(world, 10, 64, -5, 37, -12);
        Location before = input.clone();
        Particle.DustOptions data = new Particle.DustOptions(TEST_COLOR, 1);

        object.spawnParticle(input, Particle.REDSTONE, 7, 0.25, 0.5, 0.75, 0.125, data);

        Emission emission = emissions.get(0);
        assertAll(
            () -> assertEquals(before, input),
            () -> assertSame(Particle.REDSTONE, emission.particle),
            () -> assertCoordinates(emission.location, 11, 66, -2),
            () -> assertEquals(7, emission.count),
            () -> assertEquals(0.25, emission.offsetX),
            () -> assertEquals(0.5, emission.offsetY),
            () -> assertEquals(0.75, emission.offsetZ),
            () -> assertEquals(0.125, emission.extra),
            () -> assertSame(data, emission.data));
    }

    @ParameterizedTest(name = "Sphere configured color: {0}")
    @ValueSource(booleans = {false, true})
    void repeatedSphereShowsLeaveBakedLocationsAndOriginUnchanged(boolean colored)
        throws ReflectiveOperationException {
        Location origin = new Location(world, 10, 64, 10);
        Location originBefore = origin.clone();
        Sphere sphere = new Sphere(origin, 4, 1);
        sphere.setIncrementY(2);
        if (colored) {
            sphere.setColor(TEST_COLOR);
        }
        Field locationsField = Sphere.class.getDeclaredField("locations");
        locationsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Location> cache = (List<Location>) locationsField.get(sphere);
        List<Location> before = snapshot(cache);
        List<List<Location>> cacheAfterFrames = new ArrayList<>();
        List<List<Location>> frames = new ArrayList<>();

        for (int frame = 0; frame < 3; frame++) {
            emissions.clear();
            sphere.show();
            List<Location> shown = new ArrayList<>();
            for (Emission emission : emissions) {
                shown.add(emission.location.clone());
            }
            frames.add(shown);
            cacheAfterFrames.add(snapshot(cache));
        }

        assertAll(
            () -> assertEquals(originBefore, origin),
            () -> assertEquals(4, frames.get(0).size()),
            () -> assertCoordinates(frames.get(0).get(0), 10, 67, 10),
            () -> assertEquals(frames.get(0), frames.get(1), "second frame must not drift to Y=69"),
            () -> assertEquals(frames.get(0), frames.get(2), "third frame must not accumulate increments"),
            () -> assertEquals(before, cacheAfterFrames.get(0), "first show must not alter baked points"),
            () -> assertEquals(before, cacheAfterFrames.get(1), "second show must not alter baked points"),
            () -> assertEquals(before, cacheAfterFrames.get(2), "third show must not alter baked points"));
    }

    private static void render(RenderingPath path, ParticleObject object, Location input) {
        switch (path) {
            case ORDINARY:
                object.spawnParticle(input);
                break;
            case EXPLICIT_COLOR:
                object.spawnColorParticle(input, TEST_COLOR.getRed(), TEST_COLOR.getGreen(), TEST_COLOR.getBlue());
                break;
            case CONFIGURED_COLOR:
                object.setColor(TEST_COLOR);
                object.spawnParticle(input);
                break;
            default:
                throw new AssertionError(path);
        }
    }

    private void assertEmission(Emission emission, RenderingPath path, double x, double y, double z) {
        assertCoordinates(emission.location, x, y, z);
        if (path == RenderingPath.ORDINARY) {
            assertSame(Particle.FLAME, emission.particle);
        } else {
            assertSame(Particle.REDSTONE, emission.particle);
            assertTrue(emission.data instanceof Particle.DustOptions);
            assertEquals(TEST_COLOR, ((Particle.DustOptions) emission.data).getColor());
        }
    }

    private void assertCoordinates(Location location, double x, double y, double z) {
        assertAll(
            () -> assertSame(world, location.getWorld()),
            () -> assertEquals(x, location.getX(), 1.0e-9),
            () -> assertEquals(y, location.getY(), 1.0e-9),
            () -> assertEquals(z, location.getZ(), 1.0e-9));
    }

    private static List<Location> snapshot(List<Location> locations) {
        List<Location> copy = new ArrayList<>();
        for (Location location : locations) {
            copy.add(location.clone());
        }
        return copy;
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
            (instance, method, arguments) -> {
                if (method.getDeclaringClass() == Object.class) {
                    switch (method.getName()) {
                        case "equals":
                            return instance == arguments[0];
                        case "hashCode":
                            return System.identityHashCode(instance);
                        case "toString":
                            return "Test" + type.getSimpleName();
                        default:
                            throw new AssertionError(method);
                    }
                }
                return handler.invoke(instance, method, arguments);
            }));
    }

    private enum RenderingPath {
        ORDINARY, EXPLICIT_COLOR, CONFIGURED_COLOR
    }

    private static final class ParticleProbe extends ParticleObject {
        private ParticleProbe() {
            super(Particle.FLAME);
        }

        @Override
        public List<Location> calculateLocations() {
            return Collections.emptyList();
        }

        @Override
        public void show() {
            // Tests call the inherited production spawn methods directly.
        }
    }

    private static final class Emission {
        private final Particle particle;
        private final Location location;
        private final int count;
        private final double offsetX;
        private final double offsetY;
        private final double offsetZ;
        private final double extra;
        private final Object data;

        private Emission(Particle particle, Location location, int count, double offsetX,
                         double offsetY, double offsetZ, double extra, Object data) {
            this.particle = particle;
            this.location = location;
            this.count = count;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.extra = extra;
            this.data = data;
        }
    }
}
