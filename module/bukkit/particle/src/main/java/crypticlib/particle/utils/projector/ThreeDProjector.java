package crypticlib.particle.utils.projector;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * 表示一个三维至三维投影器
 * <p>算法由 @Bryan33 提供</p>
 *
 * @author Zoyn
 * @since 2020/9/19
 */
public class ThreeDProjector {

    private final Location origin;
    private final Vector n1;
    private final Vector n2;
    private final Vector n3;

    /**
     * @param origin 投影的原点
     * @param n      投影屏幕的法向量
     */
    public ThreeDProjector(Location origin, Vector n) {
        this.origin = origin;
        this.n1 = computeN1(n);
        this.n2 = n1.clone().crossProduct(n).normalize();
        this.n3 = n.clone().normalize();
    }

    /**
     * 构造与法向量正交的第一个基向量
     * <p>默认使用 Y 轴作为辅助轴与法向量做叉积; 当法向量与 Y 轴平行时(如
     * 图形平铺在地面/天花板上), 叉积会退化为零向量, 归一化后得到 NaN,
     * 此时改用 X 轴作为辅助轴。</p>
     *
     * @param n 投影屏幕的法向量
     * @return 与法向量正交的单位向量
     */
    private static Vector computeN1(Vector n) {
        Vector t = n.clone();
        t.setY(t.getY() + 1);
        Vector n1 = n.clone().crossProduct(t);
        if (n1.lengthSquared() < 1.0E-8) {
            t = n.clone();
            t.setX(t.getX() + 1);
            n1 = n.clone().crossProduct(t);
        }
        return n1.normalize();
    }

    public Location apply(double x, double y, double z) {
        Vector r = n1.clone().multiply(x).add(n2.clone().multiply(z)).add(n3.clone().multiply(y));
        return origin.clone().add(r);
    }

}
