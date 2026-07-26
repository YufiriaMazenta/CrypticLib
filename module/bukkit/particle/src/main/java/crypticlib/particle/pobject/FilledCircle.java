package crypticlib.particle.pobject;

import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示一个实心圆
 *
 * @author Zoyn
 */
public class FilledCircle extends ParticleObject implements Playable {

    private final double radius;
    // 粒子数量
    private final int sample;
    // 动画中当前的第 currentCount 个粒子
    private int currentCount;

    /**
     * 构造一个实心圆
     *
     * @param origin 原点坐标
     * @param radius 半径大小
     * @param sample 粒子数量
     */
    public FilledCircle(Location origin, double radius, int sample) {
        setOriginLocation(origin);
        this.radius = radius;
        this.sample = sample;

        this.currentCount = 0;
    }

    @Override
    public void show() {
        for (int i = 0; i < sample; i++) {
            double indices = i + 0.5;
            double r = Math.sqrt(indices / sample);
            double theta = Math.PI * (1 + Math.sqrt(5)) * indices;
            double x = radius * r * Math.cos(theta);
            double z = radius * r * Math.sin(theta);

            Location spawnLocation = getOriginLocation().clone().add(x, 0, z);
            spawnParticle(spawnLocation);
        }
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < sample; i++) {
            double indices = i + 0.5;
            double r = Math.sqrt(indices / sample);
            double theta = Math.PI * (1 + Math.sqrt(5)) * indices;
            double x = radius * r * Math.cos(theta);
            double z = radius * r * Math.sin(theta);

            Location showLocation = getOriginLocation().clone().add(x, 0, z);
            if (hasMatrix()) {
                Vector vector = new Vector(x, 0 ,z);
                Vector changed = matrix().applyVector(vector);

                showLocation = getOriginLocation().clone().add(changed);
            }

            showLocation.add(incrementX(), incrementY(), incrementZ());
            locations.add(showLocation);
//            Location spawnLocation = getOrigin().clone().add(x, 0, z);
//            locations.add(spawnLocation);
        }
        return locations;
    }

    /**
     * 获得实心圆中所有点的Location
     *
     * @param origin 原点
     * @param count  个数
     * @return 粒子播放的点
     */
    public List<Location> calculateLocations(Location origin, long count) {
        List<Location> locations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double indices = i + 0.5;
            double r = Math.sqrt(indices / count);
            double theta = Math.PI * (1 + Math.sqrt(5)) * indices;
            double x = radius * r * Math.cos(theta);
            double z = radius * r * Math.sin(theta);

            Location spawnLocation = origin.clone().add(x, 0, z);
            locations.add(spawnLocation);
        }
        return locations;
    }

    @Override
    public void play() {
        // 每次播放前重置游标, 并登记任务到 showTask 以便 turnOffTask 取消
        currentCount = 0;
        showTask = new CrypticLibRunnable() {
            @Override
            public void run() {
                if (currentCount > sample) {
                    cancel();
                    return;
                }
                double indices = currentCount + 0.5;
                double r = Math.sqrt(indices / sample);
                double theta = Math.PI * (1 + Math.sqrt(5)) * indices;
                double x = radius * r * Math.cos(theta);
                double z = radius * r * Math.sin(theta);

                spawnParticle(getOriginLocation().clone().add(x, 0, z));
                currentCount++;
            }
        }.syncTimer(0, period());
    }

    @Override
    public void playNextPoint() {
        currentCount++;
        double indices = currentCount + 0.5;
        double r = Math.sqrt(indices / sample);
        double theta = Math.PI * (1 + Math.sqrt(5)) * indices;
        double x = radius * r * Math.cos(theta);
        double z = radius * r * Math.sin(theta);

        spawnParticle(getOriginLocation().clone().add(x, 0, z));

        // 进行重置
        if (currentCount > sample) {
            currentCount = 0;
        }
    }

    /**
     * 使用给定的时间播放粒子
     *
     * @param time  持续时间, 单位 tick
     * @param count 粒子数量
     */
    public void playWithTime(long time, long count) {
        if (time == 0) {
            for (int i = 0; i < count; i++) {
                double indices = i + 0.5;
                double r = Math.sqrt(indices / count);
                double theta = Math.PI * (1 + Math.sqrt(5)) * indices;
                double x = radius * r * Math.cos(theta);
                double z = radius * r * Math.sin(theta);

                Location spawnLocation = getOriginLocation().clone().add(x, 0, z);
                spawnParticle(spawnLocation);
            }
            return;
        }

        new CrypticLibRunnable() {
            // 这里用来计量当前要播放的粒子是第几个tick, 也可说是帧数
            int frame = 0;
            // 已经绘制到的粒子下标(不含), 每帧从此处继续, 保证区间不重叠
            long sample = 0;

            @Override
            public void run() {
                if (frame >= time) {
                    cancel();
                    return;
                }
                frame++;
                // 每一帧要计算的粒子数量, 至少为 1, 避免 count<time 时空转
                int frameTick = (int) (count / time);
                if (frameTick < 1) {
                    frameTick = 1;
                }
                long upper = (long) frame * frameTick;
                // 最后一帧补齐余数, 保证绘制到 count 为止
                if (frame >= time) {
                    upper = count;
                }
                for (long i = sample; i < upper && i < count; i++) {
                    double indices = i + 0.5;
                    double r = Math.sqrt(indices / count);
                    double theta = Math.PI * (1 + Math.sqrt(5)) * indices;
                    double x = radius * r * Math.cos(theta);
                    double z = radius * r * Math.sin(theta);


                    Location spawnLocation = getOriginLocation().clone().add(x, 0, z);
                    spawnParticle(spawnLocation);
                }
                sample = Math.min(upper, count);
            }
        }.syncTimer(0L, 1L);
    }

}
