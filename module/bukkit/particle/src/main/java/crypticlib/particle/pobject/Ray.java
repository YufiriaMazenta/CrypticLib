package crypticlib.particle.pobject;

import com.google.common.collect.Lists;
import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 代表一个射线
 * <p>
 * 注意: 当 {@link RayStopType#HIT_ENTITY} 时, show()/play()/playNextPoint() 会调用
 * {@link org.bukkit.World#getNearbyEntities} 检测实体。该方法在 Spigot/Paper 上有异步检查,
 * 请勿对使用 HIT_ENTITY 的 Ray 调用 alwaysShowAsync/alwaysPlayAsync, 否则会在异步线程抛出异常;
 * 实体命中回调也应仅在同步线程操作实体。
 *
 * @author Zoyn IceCold
 */
public class Ray extends ParticleObject implements Playable {

    private Vector direction;
    private double maxLength;
    private double step;
    /**
     * 用于检测实体时获取周围实体的范围
     */
    private double range;
    private RayStopType stopType;
    private Consumer<Entity> hitEntityConsumer;
    private Predicate<Entity> entityFilter;

    private double currentStep = 0D;

    public Ray(Location origin, Vector direction, double maxLength) {
        this(origin, direction, maxLength, 0.2D);
    }

    public Ray(Location origin, Vector direction, double maxLength, double step) {
        this(origin, direction, maxLength, step, 0.5D, RayStopType.MAX_LENGTH, null);
    }

    public Ray(Location origin, Vector direction, double maxLength, double step, double range, RayStopType stopType, Consumer<Entity> hitEntityConsumer) {
        this(origin, direction, maxLength, step, range, stopType, hitEntityConsumer, null);
    }

    public Ray(Location origin, Vector direction, double maxLength, double step, double range, RayStopType stopType, Consumer<Entity> hitEntityConsumer, Predicate<Entity> entityFilter) {
        setOriginLocation(origin);
        this.direction = direction;
        this.maxLength = maxLength;
        this.step = step;
        this.range = range;
        this.stopType = stopType;
        this.hitEntityConsumer = hitEntityConsumer;
        this.entityFilter = entityFilter;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();

        for (double i = 0; i < maxLength; i += step) {
            Vector vectorTemp = direction.clone().multiply(i);
            Location spawnLocation = getOriginLocation().clone().add(vectorTemp);

            points.add(spawnLocation);

            if (stopType.equals(RayStopType.HIT_ENTITY)) {
                Collection<Entity> nearbyEntities = spawnLocation.getWorld().getNearbyEntities(spawnLocation, range, range, range);
                List<Entity> entities = Lists.newArrayList();
                // 检测有无过滤器
                if (entityFilter != null) {
                    for (Entity entity : nearbyEntities) {
                        if (!entityFilter.test(entity)) {
                            entities.add(entity);
                        }
                    }
                } else {
                    entities = new ArrayList<>(nearbyEntities);
                }

                // 获取首个实体
                if (!entities.isEmpty()) {
                    break;
                }
            }
        }

        // 做一个对 Matrix 和 Increment 的兼容
        return points.stream().map(location -> {
            Location showLocation = location;
            if (hasMatrix()) {
                Vector v = new Vector(location.getX() - getOriginLocation().getX(), location.getY() - getOriginLocation().getY(), location.getZ() - getOriginLocation().getZ());
                Vector changed = matrix().applyVector(v);

                showLocation = getOriginLocation().clone().add(changed);
            }

            showLocation.add(incrementX(), incrementY(), incrementZ());
            return showLocation;
        }).collect(Collectors.toList());
    }

    @Override
    public void show() {
        for (double i = 0; i < maxLength; i += step) {
            Vector vectorTemp = direction.clone().multiply(i);
            Location spawnLocation = getOriginLocation().clone().add(vectorTemp);

            spawnParticle(spawnLocation);

            if (stopType.equals(RayStopType.HIT_ENTITY)) {
                Collection<Entity> nearbyEntities = spawnLocation.getWorld().getNearbyEntities(spawnLocation, range, range, range);
                List<Entity> entities = Lists.newArrayList();
                // 检测有无过滤器
                if (entityFilter != null) {
                    for (Entity entity : nearbyEntities) {
                        if (!entityFilter.test(entity)) {
                            entities.add(entity);
                        }
                    }
                } else {
                    entities = new ArrayList<>(nearbyEntities);
                }

                // 获取首个实体
                if (!entities.isEmpty()) {
                    if (hitEntityConsumer != null) {
                        hitEntityConsumer.accept(entities.get(0));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void play() {
        // 每次播放前重置游标, 并登记任务到 showTask 以便 turnOffTask 取消
        currentStep = 0D;
        showTask = new CrypticLibRunnable() {
            @Override
            public void run() {
                // 进行关闭
                if (currentStep > maxLength) {
                    cancel();
                    return;
                }
                Vector vectorTemp = direction.clone().multiply(currentStep);
                Location spawnLocation = getOriginLocation().clone().add(vectorTemp);

                spawnParticle(spawnLocation);
                currentStep += step;

                if (stopType.equals(RayStopType.HIT_ENTITY)) {
                    Collection<Entity> nearbyEntities = spawnLocation.getWorld().getNearbyEntities(spawnLocation, range, range, range);
                    List<Entity> entities = Lists.newArrayList();
                    // 检测有无过滤器
                    if (entityFilter != null) {
                        for (Entity entity : nearbyEntities) {
                            if (!entityFilter.test(entity)) {
                                entities.add(entity);
                            }
                        }
                    } else {
                        entities = new ArrayList<>(nearbyEntities);
                    }

                    // 获取首个实体
                    if (!entities.isEmpty()) {
                        if (hitEntityConsumer != null) {
                            hitEntityConsumer.accept(entities.get(0));
                        }
                        cancel();
                    }
                }
            }
        }.syncTimer(0, period());
    }

    @Override
    public void playNextPoint() {
        currentStep += step;
        Vector vectorTemp = direction.clone().multiply(currentStep);
        Location spawnLocation = getOriginLocation().clone().add(vectorTemp);

        spawnParticle(spawnLocation);

        if (stopType.equals(RayStopType.HIT_ENTITY)) {
            Collection<Entity> nearbyEntities = spawnLocation.getWorld().getNearbyEntities(spawnLocation, range, range, range);
            List<Entity> entities = Lists.newArrayList();
            // 检测有无过滤器
            if (entityFilter != null) {
                for (Entity entity : nearbyEntities) {
                    if (!entityFilter.test(entity)) {
                        entities.add(entity);
                    }
                }
            } else {
                entities = new ArrayList<>(nearbyEntities);
            }

            // 获取首个实体
            if (!entities.isEmpty()) {
                if (hitEntityConsumer != null) {
                    hitEntityConsumer.accept(entities.get(0));
                }
                return;
            }
        }

        if (currentStep > maxLength) {
            currentStep = 0D;
        }
    }

    public Vector direction() {
        return direction;
    }

    public Ray setDirection(Vector direction) {
        this.direction = direction;
        return this;
    }

    public double maxLength() {
        return maxLength;
    }

    public Ray setMaxLength(double maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public double step() {
        return step;
    }

    public Ray setStep(double step) {
        this.step = step;
        return this;
    }

    public double range() {
        return range;
    }

    public Ray setRange(double range) {
        this.range = range;
        return this;
    }

    public RayStopType stopType() {
        return stopType;
    }

    public Ray setStopType(RayStopType stopType) {
        this.stopType = stopType;
        return this;
    }

    public Consumer<Entity> hitEntityConsumer() {
        return hitEntityConsumer;
    }

    public Ray setHitEntityConsumer(Consumer<Entity> hitEntityConsumer) {
        this.hitEntityConsumer = hitEntityConsumer;
        return this;
    }

    public Predicate<Entity> entityFilter() {
        return entityFilter;
    }

    /**
     * 设置实体过滤器
     * <p>
     * 注意: 该过滤器的语义为"排除", 即 {@code test} 返回 {@code true} 的实体会被排除在命中候选之外,
     * 与 {@link Stream#filter} 保留 true 的惯例相反。
     *
     * @param entityFilter 实体过滤器(返回 true 表示排除该实体)
     * @return {@link Ray}
     */
    public Ray setEntityFilter(Predicate<Entity> entityFilter) {
        this.entityFilter = entityFilter;
        return this;
    }

    public enum RayStopType {
        /**
         * 固定长度(同时也是最大长度)
         */
        MAX_LENGTH,
        /**
         * 碰撞至实体时停止
         */
        HIT_ENTITY,
    }

}
