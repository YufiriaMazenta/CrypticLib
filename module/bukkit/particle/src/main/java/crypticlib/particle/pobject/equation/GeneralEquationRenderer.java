package crypticlib.particle.pobject.equation;

import com.google.common.collect.Lists;
import crypticlib.particle.pobject.ParticleObject;
import crypticlib.particle.pobject.Playable;
import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表示一个普通方程渲染器
 *
 * @author Zoyn
 */
public class GeneralEquationRenderer extends ParticleObject implements Playable {

    private final Function<Double, Double> function;
    private double minX;
    private double maxX;
    private double dx;
    private double currentX;

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function) {
        this(origin, function, -5D, 5D);
    }

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function, double minX, double maxX) {
        this(origin, function, minX, maxX, 0.1);
    }

    public GeneralEquationRenderer(Location origin, Function<Double, Double> function, double minX, double maxX, double dx) {
        setOriginLocation(origin);
        this.function = function;
        this.minX = minX;
        this.maxX = maxX;
        this.dx = dx;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();
        for (double x = minX; x < maxX; x += dx) {
            points.add(getOriginLocation().clone().add(x, function.apply(x), 0));
        }
        // 做一个对 Matrix 和 Increment 的兼容
        return points.stream().map(location -> {
            Location showLocation = location;
            if (hasMatrix()) {
                Vector v = new Vector(location.getX() - getOriginLocation().getX(), location.getY() - getOriginLocation().getY(), location.getZ() - getOriginLocation().getZ());
                Vector changed = getMatrix().applyVector(v);

                showLocation = getOriginLocation().clone().add(changed);
            }

            showLocation.add(getIncrementX(), getIncrementY(), getIncrementZ());
            return showLocation;
        }).collect(Collectors.toList());
    }

    @Override
    public void show() {
        for (double x = minX; x < maxX; x += dx) {
            spawnParticle(getOriginLocation().clone().add(x, function.apply(x), 0));
        }
    }

    @Override
    public void play() {
        new CrypticLibRunnable() {
            @Override
            public void run() {
                // 进行关闭
                if (currentX > maxX) {
                    cancel();
                    return;
                }
                currentX += dx;
                spawnParticle(getOriginLocation().clone().add(currentX, function.apply(currentX), 0));
            }
        }.syncTimer(0, getPeriod());
    }

    @Override
    public void playNextPoint() {
        // 进行关闭
        if (currentX > maxX) {
            currentX = minX;
        }
        currentX += dx;
        spawnParticle(getOriginLocation().clone().add(currentX, function.apply(currentX), 0));
    }

    public double getMinX() {
        return minX;
    }

    public GeneralEquationRenderer setMinX(double minX) {
        this.minX = minX;
        return this;
    }

    public double getMaxX() {
        return maxX;
    }

    public GeneralEquationRenderer setMaxX(double maxX) {
        this.maxX = maxX;
        return this;
    }

    public double getDx() {
        return dx;
    }

    public GeneralEquationRenderer setDx(double dx) {
        this.dx = dx;
        return this;
    }
}
