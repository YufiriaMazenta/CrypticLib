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
 * 表示一个极坐标方程渲染器
 *
 * @author Zoyn
 */
public class PolarEquationRenderer extends ParticleObject implements Playable {

    private final Function<Double, Double> function;
    private double minTheta;
    private double maxTheta;
    private double dTheta;
    private double currentTheta;

    /**
     * 极坐标渲染器
     *
     * @param origin   原点
     * @param function 极坐标方程
     */
    public PolarEquationRenderer(Location origin, Function<Double, Double> function) {
        this(origin, function, 0D, 360D, 1D);
    }

    /**
     * 极坐标渲染器
     *
     * @param origin   原点
     * @param function 极坐标方程
     * @param minTheta 自变量最小值
     * @param maxTheta 自变量最大值
     * @param dTheta   每次自变量所增加的量
     */
    public PolarEquationRenderer(Location origin, Function<Double, Double> function, double minTheta, double maxTheta, double dTheta) {
        setOriginLocation(origin);
        this.function = function;
        this.minTheta = minTheta;
        this.maxTheta = maxTheta;
        this.dTheta = dTheta;
    }

    @Override
    public List<Location> calculateLocations() {
        List<Location> points = Lists.newArrayList();
        for (double theta = minTheta; theta < maxTheta; theta += dTheta) {
            double rho = function.apply(theta);
            double rad = Math.toRadians(theta);
            double x = rho * Math.cos(rad);
            double y = rho * Math.sin(rad);
            points.add(getOriginLocation().clone().add(x, y, 0));
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
        for (double theta = minTheta; theta < maxTheta; theta += dTheta) {
            double rho = function.apply(theta);
            double rad = Math.toRadians(theta);
            double x = rho * Math.cos(rad);
            double y = rho * Math.sin(rad);
            spawnParticle(getOriginLocation().clone().add(x, y, 0));
        }
    }

    @Override
    public void play() {
        new CrypticLibRunnable() {
            @Override
            public void run() {
                // 进行关闭
                if (currentTheta > maxTheta) {
                    cancel();
                    return;
                }
                currentTheta += dTheta;

                double rho = function.apply(currentTheta);
                double rad = Math.toRadians(currentTheta);
                double x = rho * Math.cos(rad);
                double y = rho * Math.sin(rad);
                spawnParticle(getOriginLocation().clone().add(x, y, 0));
            }
        }.syncTimer(0, getPeriod());
    }

    @Override
    public void playNextPoint() {
        // 进行关闭
        if (currentTheta > maxTheta) {
            currentTheta = minTheta;
        }
        currentTheta += dTheta;

        double rho = function.apply(currentTheta);
        double rad = Math.toRadians(currentTheta);
        double x = rho * Math.cos(rad);
        double y = rho * Math.sin(rad);
        spawnParticle(getOriginLocation().clone().add(x, y, 0));
    }

    public double getMinTheta() {
        return minTheta;
    }

    public PolarEquationRenderer setMinTheta(double minTheta) {
        this.minTheta = minTheta;
        return this;
    }

    public double getMaxTheta() {
        return maxTheta;
    }

    public PolarEquationRenderer setMaxTheta(double maxTheta) {
        this.maxTheta = maxTheta;
        return this;
    }

    public double getDTheta() {
        return dTheta;
    }

    public PolarEquationRenderer setDTheta(double dTheta) {
        this.dTheta = dTheta;
        return this;
    }
}
