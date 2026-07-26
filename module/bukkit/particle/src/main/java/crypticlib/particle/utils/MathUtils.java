package crypticlib.particle.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MathUtils {

    private static final Random RANDOM = new Random();

    public static Random random() {
        return RANDOM;
    }

    public static double randomDouble() {
        return RANDOM.nextDouble();
    }

    public static double uniformRandom(double a, double b) {
        return a + (b - a) * RANDOM.nextDouble();
    }

    /**
     * 根据给定的起始和终止点创建一个等间距的数字范围
     * <p>
     * 类似 NumPy 的 arange 方法
     * @param start 起始值
     * @param stop 结束值
     * @param step 步进长度, 不能为 0
     * @return 所有数字的列表; 当步进方向与区间方向相反时返回空列表
     * @throws IllegalArgumentException 当 step 为 0 时抛出
     */
    public static List<Double> arange(double start, double stop, double step) {
        if (step == 0) {
            throw new IllegalArgumentException("步进长度不能为 0");
        }
        List<Double> data = new ArrayList<>();
        double range = stop - start;
        // 步进方向与区间方向相反(或区间长度为 0)时返回空列表, 与 NumPy 语义一致
        if ((range > 0) != (step > 0)) {
            return data;
        }
        // 先计算元素个数再用 start + i * step 生成, 避免浮点累积误差
        int n = (int) Math.ceil(range / step);
        for (int i = 0; i < n; i++) {
            data.add(start + i * step);
        }
        return data;
    }
}
