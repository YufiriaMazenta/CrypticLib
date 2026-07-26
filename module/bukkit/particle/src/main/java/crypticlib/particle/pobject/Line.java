package crypticlib.particle.pobject;

import com.google.common.collect.Lists;
import crypticlib.MinecraftVersion;
import crypticlib.scheduler.CrypticLibRunnable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * 表示一条线
 *
 * @author Zoyn IceCold
 */
public class Line extends ParticleObject implements Playable {

    private Vector vector;
    private Location start;
    private Location end;
    /**
     * 步长
     */
    private double step;
    /**
     * 向量长度
     */
    private double length;
    private double currentStep = 0D;

    public Line(Location start, Location end) {
        this(start, end, 0.1);
    }

    /**
     * 构造一个线
     *
     * @param start 线的起点
     * @param end   线的终点
     * @param step  每个粒子之间的间隔 (也即步长)
     */
    public Line(Location start, Location end, double step) {
        this(start, end, step, 20L);
    }

    /**
     * 构造一个线
     *
     * @param start  线的起点
     * @param end    线的终点
     * @param step   每个粒子之间的间隔 (也即步长)
     * @param period 特效周期(如果需要可以使用)
     */
    public Line(Location start, Location end, double step, long period) {
        this.start = start;
        this.end = end;
        this.step = step;
        setPeriod(period);
        // 设定原点为起点, 否则叠加矩阵后 spawnParticle 会因 originLocation 为 null 而 NPE
        setOriginLocation(start);

        // 对向量进行重置
        resetVector();
    }

    public static void buildLine(Location locA, Location locB, double step, Particle particle) {
        Vector vectorAB = locB.clone().subtract(locA).toVector();
        double vectorLength = vectorAB.length();
        vectorAB.normalize();
        for (double i = 0; i < vectorLength; i += step) {
            locA.getWorld().spawnParticle(particle, locA.clone().add(vectorAB.clone().multiply(i)), 1);
        }
    }

    public static void buildLine(Location locA, Location locB, double step, Color color) {
        Vector vectorAB = locB.clone().subtract(locA).toVector();
        double vectorLength = vectorAB.length();
        vectorAB.normalize();
        boolean after113 = MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_13);
        for (double i = 0; i < vectorLength; i += step) {
            Location loc = locA.clone().add(vectorAB.clone().multiply(i));
            if (after113) {
                // 1.13+ 中 REDSTONE/DUST 的 data 必须是 DustOptions, 直接传 Color 会抛异常
                Particle.DustOptions dust = new Particle.DustOptions(color, 1);
                loc.getWorld().spawnParticle(REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, 0, 0, 0, 1, dust);
            } else {
                // 低版本走 count=0 + offset 编码颜色的兼容路径
                if (color.getRed() == 0 && color.getBlue() == 0 && color.getGreen() == 0) {
                    loc.getWorld().spawnParticle(REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, Float.MIN_VALUE / 255.0f, Float.MIN_VALUE / 255.0f, Float.MIN_VALUE / 255.0f, 1);
                } else {
                    loc.getWorld().spawnParticle(REDSTONE, loc.getX(), loc.getY(), loc.getZ(), 0, color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1);
                }
            }
        }
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();
        for (double i = 0; i < length; i += step) {
            Vector vectorTemp = vector.clone().multiply(i);
            points.add(start.clone().add(vectorTemp));
        }
        return points;
    }

    @Override
    public void show() {
        for (double i = 0; i < length; i += step) {
            Vector vectorTemp = vector.clone().multiply(i);
            spawnParticle(start.clone().add(vectorTemp));
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
                if (currentStep > length) {
                    cancel();
                    return;
                }
                Vector vectorTemp = vector.clone().multiply(currentStep);
                spawnParticle(start.clone().add(vectorTemp));
                currentStep += step;
            }
        }.syncTimer(0, period());
    }

    @Override
    public void playNextPoint() {
        currentStep += step;
        Vector vectorTemp = vector.clone().multiply(currentStep);
        spawnParticle(start.clone().add(vectorTemp));

        if (currentStep > length) {
            currentStep = 0D;
        }
    }

    public Location start() {
        return start;
    }

    public Line setStart(Location start) {
        this.start = start;
        resetVector();
        return this;
    }

    public Location end() {
        return end;
    }

    public Line setEnd(Location end) {
        this.end = end;
        resetVector();
        return this;
    }

    public double step() {
        return step;
    }

    public Line setStep(double step) {
        this.step = step;
        resetVector();
        return this;
    }

    public void resetVector() {
        vector = end.clone().subtract(start).toVector();
        length = vector.length();
        vector.normalize();
    }

}
