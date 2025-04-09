package crypticlib.particle.pobject;

import crypticlib.CrypticLibBukkit;
import crypticlib.MinecraftVersion;
import crypticlib.particle.utils.matrix.Matrix;
import crypticlib.scheduler.task.TaskWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 表示一个特效对象
 *
 * @author Zoyn IceCold
 */
public abstract class ParticleObject {

    protected static final Particle REDSTONE;//因为1.20.5以上REDSTONE更名为DUST,需要做一些小小的兼容
    protected Location originLocation;

    protected ShowType showType = ShowType.NONE;
    protected TaskWrapper showTask;
    protected long period;
    protected boolean running = false;

    protected Particle particle;
    protected int count = 1;
    protected double offsetX = 0;
    protected double offsetY = 0;
    protected double offsetZ = 0;
    protected double extra = 0;
    protected Object data = null;
    protected Color color;
    protected UUID entityId;
    /**
     * X的变化量
     */
    protected double incrementX;
    protected double incrementY;
    protected double incrementZ;
    /**
     * 表示该特效对象所拥有的矩阵
     */
    protected Matrix matrix;

    static {
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_20_5)) {
            REDSTONE = Particle.valueOf("DUST");
        } else {
            REDSTONE = Particle.valueOf("REDSTONE");
        }
    }

    public ParticleObject() {
        //1.20.5以上VILLAGER_HAPPY更名为HAPPY_VILLAGER,做一些小小的兼容
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_20_5)) {
            particle = Particle.valueOf("HAPPY_VILLAGER");
        } else {
            particle = Particle.valueOf("VILLAGER_HAPPY");
        }
    }

    public ParticleObject(Particle particle) {
        this.particle = particle;
    }

    /**
     * 将计算好的粒子展示位置以列表的方式返回
     *
     * @return 粒子位置列表
     */
    public abstract List<Location> calculateLocations();

    /**
     * 将特效对象展示
     */
    public abstract void show();

    /**
     * 将特效对象持续地展示
     */
    public void alwaysShow() {
        turnOffTask();

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        CrypticLibBukkit.scheduler().syncLater(() -> {
            running = true;
            showTask = CrypticLibBukkit.scheduler().syncTimer(() -> {
                if (!running) {
                    return;
                }
                show();
            }, 0L, period);
            setShowType(ShowType.ALWAYS_SHOW);
        }, 2L);
    }

    /**
     * 将特效对象持续地异步地展示
     */
    public void alwaysShowAsync() {
        turnOffTask();

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        CrypticLibBukkit.scheduler().syncLater(() -> {
            running = true;
            showTask = CrypticLibBukkit.scheduler().asyncTimer(() -> {
                if (!running) {
                    return;
                }
                show();
            }, 0L, period);

            setShowType(ShowType.ALWAYS_SHOW_ASYNC);
        }, 2L);
    }

    /**
     * 将特效对象持续地播放
     */
    public void alwaysPlay() {
        if (!(this instanceof Playable)) {
            try {
                throw new NoSuchMethodException("该粒子特效不支持播放!");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }
        }
        Playable playable = (Playable) this;
        turnOffTask();

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        CrypticLibBukkit.scheduler().syncLater(() -> {
            running = true;
            showTask = CrypticLibBukkit.scheduler().syncTimer(() -> {
                if (!running) {
                    return;
                }
                playable.playNextPoint();
            }, 0L, period);
            setShowType(ShowType.ALWAYS_PLAY);
        }, 2L);
    }

    /**
     * 将特效对象持续地异步地播放
     */
    public void alwaysPlayAsync() {
        if (!(this instanceof Playable)) {
            try {
                throw new NoSuchMethodException("该粒子特效不支持播放!");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return;
            }
        }
        Playable playable = (Playable) this;
        turnOffTask();

        // 此处的延迟 2tick 是为了防止turnOffTask还没把特效给关闭时的缓冲
        CrypticLibBukkit.scheduler().syncLater(() -> {
            running = true;
            showTask = CrypticLibBukkit.scheduler().asyncTimer(() -> {
                if (!running) {
                    return;
                }
                playable.playNextPoint();
            }, 0L, period);
            setShowType(ShowType.ALWAYS_PLAY_ASYNC);
        }, 2L);
    }

    /**
     * 将正在展示或播放的特效关闭
     */
    public void turnOffTask() {
        if (showTask != null) {
            running = false;
            showTask.cancel();
            setShowType(ShowType.NONE);
        }
    }

    /**
     * 给该特效对象叠加一个矩阵
     *
     * @param matrix 给定的矩阵
     * @return {@link ParticleObject}
     */
    public ParticleObject addMatrix(Matrix matrix) {
        if (this.matrix == null) {
            setMatrix(matrix);
            return this;
        }
        this.matrix = matrix.multiply(this.matrix);
        return this;
    }

    /**
     * 移除该特效对象的矩阵
     *
     * @return {@link ParticleObject}
     */
    public ParticleObject removeMatrix() {
        matrix = null;
        return this;
    }

    /**
     * 判断该特效对象是否拥有矩阵
     *
     * @return 当拥有矩阵时返回 true
     */
    public boolean hasMatrix() {
        return matrix != null;
    }

    /**
     * 得到该特效对象的矩阵
     *
     * @return {@link Matrix}
     */
    public Matrix getMatrix() {
        return matrix;
    }

    /**
     * 给该特效对象设置一个矩阵
     * <p>
     * 该方法将会直接覆盖之前所有已经变换好的矩阵
     *
     * @param matrix 给定的矩阵
     * @return {@link ParticleObject}
     */
    public ParticleObject setMatrix(Matrix matrix) {
        this.matrix = matrix;
        return this;
    }

    /**
     * 得到 X 轴上的增量
     *
     * @return X 轴的增量
     */
    public double getIncrementX() {
        return incrementX;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时 X 轴上的增量
     * <p>
     * 换言之是在 X 轴上固定移动 incrementX 个单位
     *
     * @param incrementX X 轴的增量
     * @return {@link ParticleObject}
     */
    public ParticleObject setIncrementX(double incrementX) {
        this.incrementX = incrementX;
        return this;
    }

    /**
     * 得到 Y 轴上的增量
     *
     * @return Y 轴的增量
     */
    public double getIncrementY() {
        return incrementY;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时 Y 轴上的增量
     * <p>
     * 换言之是在 Y 轴上固定移动 incrementY 个单位
     *
     * @param incrementY Y 轴的增量
     * @return {@link ParticleObject}
     */
    public ParticleObject setIncrementY(double incrementY) {
        this.incrementY = incrementY;
        return this;
    }

    /**
     * 得到 Z 轴上的增量
     *
     * @return Z 轴的增量
     */
    public double getIncrementZ() {
        return incrementZ;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时 Z 轴上的增量
     * <p>
     * 换言之是在 Z 轴上固定移动 incrementZ 个单位
     *
     * @param incrementZ Z 轴的增量
     * @return {@link ParticleObject}
     */
    public ParticleObject setIncrementZ(double incrementZ) {
        this.incrementZ = incrementZ;
        return this;
    }

    /**
     * 获取该特效对象的原点
     * <p>
     * 当特效对象内已经 attachEntity 时则会返回该实体的 Location
     * 如果绑定的实体无法获取,将会结束播放
     *
     * @return {@link Location}
     */
    public Location getOriginLocation() {
        if (entityId != null) {
            Entity entity = getEntity();
            if (entity == null) {
                turnOffTask();
                return originLocation;
            } else {
                return entity.getLocation();
            }
        }
        return originLocation;
    }

    /**
     * 设置特效对象的原点
     *
     * @param originLocation 给定的原点
     * @return {@link ParticleObject}
     */
    public ParticleObject setOriginLocation(Location originLocation) {
        this.originLocation = originLocation;
        return this;
    }

    /**
     * 得到特效对象的 周期(period) 参数
     *
     * @return 单位为 tick
     */
    public long getPeriod() {
        return period;
    }

    /**
     * 设置特效对象的 周期(period) 参数
     *
     * @param period 给定的周期参数
     * @return {@link ParticleObject}
     */
    public ParticleObject setPeriod(long period) {
        this.period = period;
        return this;
    }

    /**
     * 得到该特效对象的展示类型
     *
     * @return {@link ShowType}
     */
    public ShowType getShowType() {
        return showType;
    }

    /**
     * 设置该特效对象的展示类型
     *
     * @param showType 给定的展示类型
     * @return {@link ParticleObject}
     */
    public ParticleObject setShowType(ShowType showType) {
        this.showType = showType;
        return this;
    }

    /**
     * 得到该特效对象的粒子类型
     *
     * @return {@link Particle}
     */
    public Particle getParticle() {
        return particle;
    }

    /**
     * 设置该特效对象的粒子类型
     * <p>
     * 该方法将会覆盖已设置的 color 参数
     *
     * @param particle 给定的粒子类型
     * @return {@link ParticleObject}
     */
    public ParticleObject setParticle(Particle particle) {
        this.particle = particle;
        // 记得重置颜色
        if (color != null) {
            color = null;
        }
        return this;
    }

    /**
     * 得到 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的粒子数量
     *
     * @return 一个点的粒子数量
     */
    public int getCount() {
        return count;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的粒子数量
     *
     * @param count 粒子数量
     * @return {@link ParticleObject}
     */
    public ParticleObject setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * 得到 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 X 轴偏移量
     *
     * @return 一个点的 X 轴偏移量
     */
    public double getOffsetX() {
        return offsetX;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 X 轴偏移量
     *
     * @param offsetX X 轴偏移量
     * @return {@link ParticleObject}
     */
    public ParticleObject setOffsetX(double offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    /**
     * 得到 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 Y 轴偏移量
     *
     * @return 一个点的 Y 轴偏移量
     */
    public double getOffsetY() {
        return offsetY;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 Y 轴偏移量
     *
     * @param offsetY Y 轴偏移量
     * @return {@link ParticleObject}
     */
    public ParticleObject setOffsetY(double offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    /**
     * 得到 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 Z 轴偏移量
     *
     * @return 一个点的 Z 轴偏移量
     */
    public double getOffsetZ() {
        return offsetZ;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 Z 轴偏移量
     *
     * @param offsetZ Z 轴偏移量
     * @return {@link ParticleObject}
     */
    public ParticleObject setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
        return this;
    }

    /**
     * 得到 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 extra 参数
     *
     * @return 一个点的 extra 参数
     */
    public double getExtra() {
        return extra;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 extra 参数
     * <p>
     * 通常为 速度
     *
     * @param extra extra 参数
     * @return {@link ParticleObject}
     */
    public ParticleObject setExtra(double extra) {
        this.extra = extra;
        return this;
    }

    /**
     * 得到 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 data 参数
     *
     * @return 一个点的 data 参数
     */
    public Object getData() {
        return data;
    }

    /**
     * 设置 {@link ParticleObject#spawnParticle(Location, Particle, int, double, double, double, double, Object)} 时的 data 参数
     *
     * @param data data 参数
     * @return {@link ParticleObject}
     */
    public ParticleObject setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * 得到当前特效对象的颜色
     *
     * @return {@link Color}
     */
    public Color getColor() {
        return color;
    }

    /**
     * 设置当前特效对象的颜色(支持 RGB)
     * <p>
     * 该方法将会覆盖 particle 的作用
     *
     * @param color 给定的颜色
     * @return {@link ParticleObject}
     */
    public ParticleObject setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * 得到当前特效对象所连接的实体的UUID
     * @return {@link UUID}
     */
    public @Nullable UUID getEntityId() {
        return entityId;
    }

    /**
     * 得到当前特效对象的所连接的实体
     * 如果当前实体/玩家死亡或退出游戏等情况,可能会返回null
     *
     * @return {@link Entity}
     */
    public @Nullable Entity getEntity() {
        return Bukkit.getEntity(entityId);
    }

    /**
     * 设置当前特效对象的所连接的实体
     * <p>
     * 该方法将会在 {@link ParticleObject#getOriginLocation()} 时自动替换成实体的实时 Location
     *
     * @param entity 给定的实体
     * @return {@link ParticleObject}
     */
    public ParticleObject attachEntity(@NotNull Entity entity) {
        this.entityId = entity.getUniqueId();
        return this;
    }

    /**
     * 通过给定一个坐标就可以使用已经指定的参数来播放粒子
     *
     * @param location 坐标
     */
    public void spawnParticle(Location location) {
        spawnParticle(location, this.particle, count, offsetX, offsetY, offsetZ, extra, data);
    }

    /**
     * 自定义程度较高的生成粒子方法
     *
     * @param location 坐标
     * @param particle 粒子
     * @param count    粒子数量
     * @param offsetX  X轴偏移量
     * @param offsetY  Y轴偏移量
     * @param offsetZ  Z轴偏移量
     * @param extra    粒子额外参数
     * @param data     特殊粒子属性
     */
    public void spawnParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
        Location showLocation = location;
        if (hasMatrix()) {
            Vector vector = location.clone().subtract(originLocation).toVector();
            Vector changed = matrix.applyVector(vector);

            showLocation = originLocation.clone().add(changed);
        }

        // 在这里可以设置一个XYZ的变化量
        showLocation.add(incrementX, incrementY, incrementZ);

        // 可以在这里设置 Color
        if (color != null) {
            if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_13)) {
                Particle.DustOptions dust = new Particle.DustOptions(color, 1);
                location.getWorld().spawnParticle(REDSTONE, showLocation.getX(), showLocation.getY(), showLocation.getZ(), 0, offsetX, offsetY, offsetZ, 1, dust);
            } else {
                // 对低版本的黑色做一个小小的兼容
                if (color.getRed() == 0 && color.getBlue() == 0 && color.getGreen() == 0) {
                    location.getWorld().spawnParticle(REDSTONE, showLocation.getX(), showLocation.getY(), showLocation.getZ(), 0, Float.MIN_VALUE / 255.0f, Float.MIN_VALUE / 255.0f, Float.MIN_VALUE / 255.0f, 1);
                } else {
                    location.getWorld().spawnParticle(REDSTONE, showLocation.getX(), showLocation.getY(), showLocation.getZ(), 0, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1);
                }
            }
            return;
        }
        location.getWorld().spawnParticle(particle, showLocation, count, offsetX, offsetY, offsetZ, extra, data);
    }

    public void spawnColorParticle(Location location, int r, int g, int b) {
        Location showLocation = location;
        if (hasMatrix()) {
            Vector vector = location.clone().subtract(originLocation).toVector();
            Vector changed = matrix.applyVector(vector);

            showLocation = originLocation.clone().add(changed);
        }

        // 在这里可以设置一个XYZ的变化量
        showLocation.add(incrementX, incrementY, incrementZ);

        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_13)) {
            Particle.DustOptions dust = new Particle.DustOptions(Color.fromRGB(r, g, b), 1);
            location.getWorld().spawnParticle(REDSTONE, showLocation.getX(), showLocation.getY(), showLocation.getZ(), 0, r, g, b, 1, dust);
        } else {
            // 对低版本的黑色做一个小小的兼容
            if (color.getRed() == 0 && color.getBlue() == 0 && color.getGreen() == 0) {
                location.getWorld().spawnParticle(REDSTONE, showLocation.getX(), showLocation.getY(), showLocation.getZ(), 0, Float.MIN_VALUE / 255.0f, Float.MIN_VALUE / 255.0f, Float.MIN_VALUE / 255.0f, 1);
            } else {
                location.getWorld().spawnParticle(REDSTONE, showLocation.getX(), showLocation.getY(), showLocation.getZ(), 0, r / 255.0f, g / 255.0f, b / 255.0f, 1);
            }
        }
    }

}