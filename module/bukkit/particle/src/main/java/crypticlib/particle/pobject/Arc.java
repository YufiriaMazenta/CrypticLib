package crypticlib.particle.pobject;

import com.google.common.collect.Lists;
import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * 表示一个弧
 *
 * @author Zoyn IceCold
 */
public class Arc extends ParticleObject implements Playable {

    private double startAngle;
    private double angle;
    private double radius;
    private double step;
    private double currentAngle = 0D;

    public Arc(Location origin) {
        this(origin, 60D);
    }

    public Arc(Location origin, double angle) {
        this(origin, 0D, angle);
    }

    public Arc(Location origin, double startAngle, double angle) {
        this(origin, startAngle, angle, 1);
    }

    public Arc(Location origin, double startAngle, double angle, double radius) {
        this(origin, startAngle, angle, radius, 1);
    }

    /**
     * 构造一个弧
     *
     * @param origin     弧所在的圆的圆点
     * @param startAngle 弧开始的角度
     * @param angle      弧所占的角度
     * @param radius     弧所在的圆的半径
     * @param step       每个粒子的间隔(也即步长)
     */
    public Arc(Location origin, double startAngle, double angle, double radius, double step) {
        this(origin, startAngle, angle, radius, step, 20L);
    }

    /**
     * 构造一个弧
     *
     * @param origin     弧所在的圆的圆点
     * @param startAngle 弧开始的角度
     * @param angle      弧所占的角度
     * @param radius     弧所在的圆的半径
     * @param step       每个粒子的间隔(也即步长)
     * @param period     特效周期(如果需要可以使用)
     */
    public Arc(Location origin, double startAngle, double angle, double radius, double step, long period) {
        setOriginLocation(origin);
        this.startAngle = startAngle;
        this.angle = angle;
        this.radius = radius;
        this.step = step;
        this.currentAngle = startAngle;
        setPeriod(period);
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();
        for (double i = startAngle; i < startAngle + angle; i += step) {
            double radians = Math.toRadians(i);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);

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
        for (double i = startAngle; i < startAngle + angle; i += step) {
            double radians = Math.toRadians(i);
            double x = radius * Math.cos(radians);
            double z = radius * Math.sin(radians);
            spawnParticle(getOriginLocation().clone().add(x, 0, z));
        }
    }

    @Override
    public void play() {
        // 每次播放前重置游标至起始角度, 并登记任务到 showTask 以便 turnOffTask 取消
        currentAngle = startAngle;
        showTask = new CrypticLibRunnable() {
            @Override
            public void run() {
                // 进行关闭
                if (currentAngle > startAngle + angle) {
                    cancel();
                    return;
                }
                double radians = Math.toRadians(currentAngle);
                double x = radius * Math.cos(radians);
                double z = radius * Math.sin(radians);

                spawnParticle(getOriginLocation().clone().add(x, 0, z));
                currentAngle += step;
            }
        }.syncTimer(0, period());
    }

    @Override
    public void playNextPoint() {
        double radians = Math.toRadians(currentAngle);
        double x = radius * Math.cos(radians);
        double z = radius * Math.sin(radians);

        spawnParticle(getOriginLocation().clone().add(x, 0, z));
        currentAngle += step;

        // 进行重置
        if (currentAngle > startAngle + angle) {
            currentAngle = startAngle;
        }
    }

    public double startAngle() {
        return startAngle;
    }

    public Arc setStartAngle(double startAngle) {
        this.startAngle = startAngle;
        return this;
    }

    public double angle() {
        return angle;
    }

    public Arc setAngle(double angle) {
        this.angle = angle;
        return this;
    }

    public double radius() {
        return radius;
    }

    public Arc setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public double step() {
        return step;
    }

    public Arc setStep(double step) {
        this.step = step;
        return this;
    }

}
