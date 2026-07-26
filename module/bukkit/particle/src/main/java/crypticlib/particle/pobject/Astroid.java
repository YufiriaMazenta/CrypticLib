package crypticlib.particle.pobject;

import com.google.common.collect.Lists;
import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * 表示一个星形线
 *
 * @author Zoyn IceCold
 */
public class Astroid extends ParticleObject implements Playable {

    private double radius;
    private double step;

    private double currentT = 0D;

    /**
     * 构造一个星形线
     * <p>
     * 该链路使用默认半径 1D, 并委托给 {@link #Astroid(double, Location)}
     *
     * @param origin 原点
     */
    public Astroid(Location origin) {
        this(1D, origin);
    }

    /**
     * 构造一个星形线
     *
     * @param radius 半径
     * @param origin 原点
     */
    public Astroid(double radius, Location origin) {
        this(radius, origin, 10);
    }

    public Astroid(double radius, Location origin, double step) {
        this.radius = radius;
        this.step = step;
        setOriginLocation(origin);
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();
        for (double t = 0.0D; t < 360.0D; t += step) {
            double radians = Math.toRadians(t);
            // 计算公式
            double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
            double z = Math.pow(this.radius * Math.sin(radians), 3.0D);

            Location showLocation = getOriginLocation().clone().add(x, 0, z);
            if (hasMatrix()) {
                Vector vector = new Vector(x, 0, z);
                Vector changed = matrix().applyVector(vector);

                showLocation = getOriginLocation().clone().add(changed);
            }

            showLocation.add(incrementX(), incrementY(), incrementZ());
            points.add(showLocation);
        }
        return points;
    }

    @Override
    public void show() {
        for (double t = 0.0D; t < 360.0D; t += step) {
            double radians = Math.toRadians(t);
            // 计算公式
            double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
            double z = Math.pow(this.radius * Math.sin(radians), 3.0D);

            spawnParticle(getOriginLocation().clone().add(x, 0, z));
        }
    }

    @Override
    public void play() {
        // 每次播放前重置游标, 并将任务登记到 showTask 以便 turnOffTask 取消
        currentT = 0D;
        showTask = new CrypticLibRunnable() {
            @Override
            public void run() {
                // 越界则关闭
                if (currentT > 360D) {
                    cancel();
                    return;
                }
                double radians = Math.toRadians(currentT);
                // 计算公式
                double x = Math.pow(radius() * Math.cos(radians), 3.0D);
                double z = Math.pow(radius() * Math.sin(radians), 3.0D);

                spawnParticle(getOriginLocation().clone().add(x, 0, z));
                currentT += step;
            }
        }.syncTimer(0, period());
    }

    @Override
    public void playNextPoint() {
        currentT += step;
        double radians = Math.toRadians(currentT);
        // 计算公式
        double x = Math.pow(this.radius * Math.cos(radians), 3.0D);
        double z = Math.pow(this.radius * Math.sin(radians), 3.0D);

        spawnParticle(getOriginLocation().clone().add(x, 0, z));
        // 重置
        if (currentT > 360D) {
            currentT = 0D;
        }
    }

    public double radius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double step() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }
}
