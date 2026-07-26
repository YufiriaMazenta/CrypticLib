package crypticlib.particle.utils.matrix;

import crypticlib.particle.utils.VectorUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Arrays;

/**
 * 表示一个 m*n 的矩阵
 * <p>在该类中, 所有的乘法操作都是左乘</p>
 *
 * @author Zoyn
 */
public class Matrix {

    private final double[][] m;

    public Matrix(int row, int column) {
        this.m = new double[row][column];
    }

    public Matrix(double[][] m) {
        this.m = m;
    }

    public Matrix(Matrix matrix) {
        this.m = matrix.asArray();
    }

    public int row() {
        return m.length;
    }

    public int column() {
        return m[0].length;
    }

    public double[][] asArray() {
        return m;
    }

    public double get(int row, int column) {
        return m[row - 1][column - 1];
    }

    /**
     * 通过给定的值设定矩阵内对应的数值
     *
     * @param row    行数
     * @param column 列数
     * @param value  数值
     * @return {@link Matrix}
     */
    public Matrix set(int row, int column, double value) {
        m[row - 1][column - 1] = value;
        return this;
    }

    /**
     * 填充矩阵的某一行为同一实数
     *
     * @param row   行数
     * @param value 实数
     * @return {@link Matrix}
     */
    public Matrix fill(int row, double value) {
        Arrays.fill(m[row - 1], value);
        return this;
    }

    /**
     * 取出矩阵中单独的一行
     *
     * @param row 行数
     * @return 对应行所成的数组
     */
    public double[] rowAsArray(int row) {
        return Arrays.copyOf(m[row - 1], column());
    }

    /**
     * 取矩阵中单独的一列
     *
     * @param column 列数
     * @return 列所成的数组
     */
    public double[] columnAsArray(int column) {
        double[] m = new double[row()];
        for (int row = 0; row < row(); row++) {
            m[row] = get(row + 1, column);
        }
        return m;
    }

    public boolean isSameRow(Matrix matrix) {
        return row() == matrix.row();
    }

    public boolean isSameColumn(Matrix matrix) {
        return column() == matrix.column();
    }

    public boolean isSameRowAndColumn(Matrix matrix) {
        return isSameRow(matrix) && isSameColumn(matrix);
    }

    /**
     * 将该矩阵进行转置变换
     *
     * @return {@link Matrix}
     */
    public Matrix invert() {
        double[][] n = new double[column()][row()];
        for (int i = 0; i < row(); i++) {
            for (int j = 0; j < column(); j++) {
                n[j][i] = m[i][j];
            }
        }
        return new Matrix(n);
    }

    /**
     * 将两个矩阵相加
     * <p>注意: 本矩阵的大小要等于另一矩阵的大小</p>
     *
     * @param matrix 给定的矩阵
     * @return {@link Matrix}
     */
    public Matrix plus(Matrix matrix) {
        if (!isSameRowAndColumn(matrix)) {
            throw new IllegalArgumentException("两矩阵大小不相同!");
        }

        double[][] n = matrix.asArray();
        double[][] result = new double[row()][column()];
        for (int row = 0; row < row(); row++) {
            for (int column = 0; column < column(); column++) {
                result[row][column] = m[row][column] + n[row][column];
            }
        }
//        this.m = result;
        return new Matrix(result);
    }

    /**
     * 将该矩阵乘以一个实数
     *
     * @param value 给定的数
     * @return {@link Matrix}
     */
    public Matrix multiply(double value) {
        double[][] result = new double[row()][column()];

        for (int row = 0; row < row(); row++) {
            for (int column = 0; column < column(); column++) {
                result[row][column] = m[row][column] * value;
            }
        }
        return new Matrix(result);
    }

    /**
     * 将该矩阵乘以另一个矩阵
     * <p>注意: 本矩阵的列数要等于另外一个矩阵的行数</p>
     *
     * @param matrix 给定的另一矩阵
     * @return {@link Matrix}
     */
    public Matrix multiply(Matrix matrix) {
        if (column() != matrix.row()) {
            throw new IllegalArgumentException("原矩阵的列数不等于新矩阵的行数");
        }

        double[][] n = matrix.asArray();
        double[][] result = new double[row()][matrix.column()];

        for (int row = 0; row < row(); row++) {
            for (int column = 0; column < matrix.column(); column++) {
                double[] x = rowAsArray(row + 1);
                double[] y = matrix.columnAsArray(column + 1);
                for (int i = 0; i < x.length; i++) {
                    result[row][column] += x[i] * y[i];
                }
            }
        }

        return new Matrix(result);
    }

    /**
     * 将矩阵漂亮的打印出来
     */
    public void prettyPrinting() {
        for (double[] doubles : m) {
            System.out.println(Arrays.toString(doubles));
        }
    }

    /**
     * 将本矩阵的变换作用至给定的坐标上
     *
     * @param location 给定的坐标
     * @param origin   原点坐标 用于确定变换的原点
     * @return {@link Location}
     */
    public Location applyLocation(Location location, Location origin) {
        Vector vector = VectorUtils.createVector(origin, location);
        return origin.clone().add(applyVector(vector));
    }

    /**
     * 将本矩阵的变换作用至给定的向量上
     *
     * @param vector 给定的向量
     * @return {@link Location}
     */
    public Vector applyVector(Vector vector) {
        if (row() == 2 && column() == 2) {
            return applyIn2DVector(vector);
        } else if (row() == 3 && column() == 3) {
            return applyIn3DVector(vector);
        }

        throw new IllegalArgumentException("当前矩阵非 2*2 或 3*3 的方阵");
    }

    private Vector applyIn2DVector(Vector vector) {
        double x = vector.getX();
        double z = vector.getZ();
        double ax = asArray()[0][0] * x;
        double ay = asArray()[0][1] * z;

        double bx = asArray()[1][0] * x;
        double by = asArray()[1][1] * z;

        return new Vector(ax + ay, vector.getY(), bx + by);
    }

    private Vector applyIn3DVector(Vector vector) {
        double x = vector.getX();
        double y = vector.getY();
        double z = vector.getZ();

        double ax = asArray()[0][0] * x;
        double ay = asArray()[0][1] * y;
        double az = asArray()[0][2] * z;

        double bx = asArray()[1][0] * x;
        double by = asArray()[1][1] * y;
        double bz = asArray()[1][2] * z;

        double cx = asArray()[2][0] * x;
        double cy = asArray()[2][1] * y;
        double cz = asArray()[2][2] * z;

        return new Vector(ax + ay + az, bx + by + bz, cx + cy + cz);
    }

}
