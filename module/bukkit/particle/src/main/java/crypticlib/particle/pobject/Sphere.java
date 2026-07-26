package crypticlib.particle.pobject;

import com.google.common.collect.Lists;
import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示一个球
 * <p>算法来源: https://stackoverflow.com/questions/9600801/evenly-distributing-n-points-on-a-sphere/26127012#26127012</p>
 *
 * @author Zoyn IceCold
 */
public class Sphere extends ParticleObject implements Playable {

    /**
     * 黄金角度 约等于137.5度
     */
    private final double phi = Math.PI * (3D - Math.sqrt(5));
    private final List<Location> locations;
    private int sample;
    private double radius;
    private int currentSample = 0;

    public Sphere(Location origin) {
        this(origin, 50, 1);
    }

    /**
     * 构造一个球
     *
     * @param origin 球的圆点
     * @param sample 样本点个数(粒子的数量)
     * @param radius 球的半径
     */
    public Sphere(Location origin, int sample, double radius) {
        setOriginLocation(origin);
        this.sample = sample;
        this.radius = radius;

        locations = new ArrayList<>();
        resetLocations();
    }

    public Sphere(Location origin, int sample, double radius, Color color) {
        this(origin, sample, radius);
    }

    @Override
    public ParticleObject setOriginLocation(Location originLocation) {
        super.setOriginLocation(originLocation);
        // 烘焙类图形在原点变化时需要重新计算点位, 否则 setOriginLocation/EffectGroup#setOrigin 不生效
        resetLocations();
        return this;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();

        for (int i = 0; i < sample; i++) {
            // y goes from 1 to -1
            double y = 1 - (i / (sample - 1f)) * 2;
            // radius at y
            double yRadius = Math.sqrt(1 - y * y);
            // golden angle increment
            double theta = phi * i;
            double x = Math.cos(theta) * radius * yRadius;
            double z = Math.sin(theta) * radius * yRadius;
            y *= radius;

            points.add(getOriginLocation().clone().add(x, y, z));
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
        locations.forEach(loc -> {
            if (loc != null) {
                spawnParticle(loc);
            }
        });
    }

    @Override
    public void play() {
        // 每次播放前重置游标至 0, 从首个点开始, 并登记任务到 showTask 以便 turnOffTask 取消
        currentSample = 0;
        showTask = new CrypticLibRunnable() {
            @Override
            public void run() {
                // 进行关闭
                if (currentSample >= locations.size()) {
                    cancel();
                    return;
                }

                spawnParticle(locations.get(currentSample));
                currentSample++;
            }
        }.syncTimer(0, period());
    }

    @Override
    public void playNextPoint() {
        // 重置
        if (currentSample + 1 == locations.size()) {
            currentSample = 0;
        }
        spawnParticle(locations.get(currentSample));
        currentSample++;
    }

    public int sample() {
        return sample;
    }

    public Sphere setSample(int sample) {
        this.sample = sample;
        resetLocations();
        return this;
    }

    public double radius() {
        return radius;
    }

    public Sphere setRadius(double radius) {
        this.radius = radius;
        resetLocations();
        return this;
    }

    /**
     * 重置烘焙点位
     * <p>
     * 该方法会在 setOriginLocation 时被调用, 使 setOriginLocation/EffectGroup#setOrigin 能生效。
     * 注意: 构造期间 super.setOriginLocation 会先于 locations 初始化被调用, 故此处对 null 做保护。
     */
    public void resetLocations() {
        if (locations == null) {
            return;
        }
        locations.clear();

        for (int i = 0; i < sample; i++) {
            // y goes from 1 to -1
            double y = 1 - (i / (sample - 1f)) * 2;
            // radius at y
            double yRadius = Math.sqrt(1 - y * y);
            // golden angle increment
            double theta = phi * i;
            double x = Math.cos(theta) * radius * yRadius;
            double z = Math.sin(theta) * radius * yRadius;
            y *= radius;

            locations.add(getOriginLocation().clone().add(x, y, z));
        }
    }
}
