package crypticlib.particle.pobject;

import com.google.common.collect.Lists;
import crypticlib.particle.utils.VectorUtils;
import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 表示一个星星
 *
 * @author Zoyn
 */
public class Star extends ParticleObject implements Playable {

    private final double length;
    private double radius;
    private double step;
    private int currentSide = 1;
    private double currentStep = 0;
    private Vector currentDirection; // 当前边的方向向量

    public Star(Location origin) {
        this(origin, 1, 0.05);
    }

    public Star(Location origin, double radius, double step) {
        setOriginLocation(origin);
        this.radius = radius;
        this.step = step;

        this.length = Math.sin(Math.toRadians(72)) * radius * 2;

        // 初始化第一条边的方向
        double angle = 72 * 3; // 初始边对应的角度
        this.currentDirection = new Vector(
            radius * Math.cos(Math.toRadians(angle)),
            0,
            radius * Math.sin(Math.toRadians(angle))
        ).subtract(new Vector(
            radius * Math.cos(Math.toRadians(72)),
            0,
            radius * Math.sin(Math.toRadians(72))
        )).normalize();
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();
        double x = radius * Math.cos(Math.toRadians(72));
        double z = radius * Math.sin(Math.toRadians(72));

        double x2 = radius * Math.cos(Math.toRadians(72 * 3));
        double z2 = radius * Math.sin(Math.toRadians(72 * 3));

        Vector START = new Vector(x2 - x, 0, z2 - z);
        START.normalize();
        Location end = getOriginLocation().clone().add(x, 0, z);

        for (int i = 1; i <= 5; i++) {
            for (double j = 0; j < length; j += step) {
                Vector vectorTemp = START.clone().multiply(j);
                Location spawnLocation = end.clone().add(vectorTemp);

                points.add(spawnLocation);
            }
            Vector vectorTemp = START.clone().multiply(length);
            end = end.clone().add(vectorTemp);

            VectorUtils.rotateAroundAxisY(START, 144);
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
        double x = radius * Math.cos(Math.toRadians(72));
        double z = radius * Math.sin(Math.toRadians(72));

        double x2 = radius * Math.cos(Math.toRadians(72 * 3));
        double z2 = radius * Math.sin(Math.toRadians(72 * 3));

        final Vector START = new Vector(x2 - x, 0, z2 - z);
        START.normalize();
        Location end = getOriginLocation().clone().add(x, 0, z);

        for (int i = 1; i <= 5; i++) {
            for (double j = 0; j < length; j += step) {
                Vector vectorTemp = START.clone().multiply(j);
                Location spawnLocation = end.clone().add(vectorTemp);

                spawnParticle(spawnLocation);
            }
            Vector vectorTemp = START.clone().multiply(length);
            end = end.clone().add(vectorTemp);

            VectorUtils.rotateAroundAxisY(START, 144);
        }
    }

    @Override
    public void play() {
        new CrypticLibRunnable() {
            // 转弧度制
            final double radians = Math.toRadians(72);
            final double x = radius * Math.cos(radians);
            final double z = radius * Math.sin(radians);
            Location end = getOriginLocation().clone().add(x, 0, z);
            final Vector START = new Vector(radius * (Math.cos(Math.toRadians(72 * 3)) - x), 0, radius * (Math.sin(Math.toRadians(72 * 3)) - z));

            @Override
            public void run() {
                // 进行关闭
                if (currentSide >= 6) {
                    cancel();
                    return;
                }
                if (currentStep > length) {
                    // 切换到下一条边开始
                    currentSide += 1;
                    currentStep = 0;

                    Vector vectorTemp = START.clone().multiply(length);
                    end = end.clone().add(vectorTemp);

                    VectorUtils.rotateAroundAxisY(START, 144);
                }
                Vector vectorTemp = START.clone().multiply(currentStep);
                Location spawnLocation = end.clone().add(vectorTemp);

                spawnParticle(spawnLocation);
                currentStep += step;
            }
        }.syncTimer(0, getPeriod());
    }

    @Override
    public void playNextPoint() {
        // 动态获取当前原点
        Location currentOrigin = getOriginLocation();

        // 计算当前边的起点（基于当前原点）
        double currentAngle = 72 + 144 * (currentSide - 1);
        Vector startPoint = new Vector(
            radius * Math.cos(Math.toRadians(currentAngle)),
            0,
            radius * Math.sin(Math.toRadians(currentAngle))
        ).add(currentOrigin.toVector());

        // 计算当前步长对应的位置
        Vector stepVector = currentDirection.clone().multiply(currentStep);
        Location spawnLocation = startPoint.toLocation(currentOrigin.getWorld()).add(stepVector);

        // 生成粒子
        spawnParticle(spawnLocation);

        currentStep += step;

        if (currentStep >= length) {
            currentSide++;
            currentStep = 0;
            // 旋转方向到下一条边（144度）
            VectorUtils.rotateAroundAxisY(currentDirection, 144);

            // 如果完成五条边，重置
            if (currentSide > 5) {
                currentSide = 1;
                // 重置方向到初始边
                double angle = 72 * 3;
                currentDirection = new Vector(
                    radius * Math.cos(Math.toRadians(angle)),
                    0,
                    radius * Math.sin(Math.toRadians(angle))
                ).subtract(new Vector(
                    radius * Math.cos(Math.toRadians(72)),
                    0,
                    radius * Math.sin(Math.toRadians(72))
                )).normalize();
            }
        }
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

}
