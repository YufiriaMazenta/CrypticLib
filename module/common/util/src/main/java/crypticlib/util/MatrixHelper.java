package crypticlib.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class MatrixHelper {

    public static <T> List<List<T>> stripEmptyBorders(List<List<T>> matrix) {
        return stripEmptyBorders(matrix, Objects::isNull, null);
    }

    /**
     * 去除边缘的空行和空列
     * @param matrix 传入的矩阵
     * @param emptyPredicate 如何判断
     * @param emptyObj 生成新的矩阵时, 用于填充空位的对象
     * @return 去除边缘空行空列后的矩阵
     * @param <T>
     */
    public static <T> List<List<T>> stripEmptyBorders(List<List<T>> matrix, Predicate<T> emptyPredicate, T emptyObj) {
        List<List<T>> rows = new ArrayList<>(matrix);

        while (!rows.isEmpty() && rows.get(0).stream().allMatch(emptyPredicate)) {
            rows.remove(0);
        }
        while (!rows.isEmpty() && rows.get(rows.size() - 1).stream().allMatch(emptyPredicate)) {
            rows.remove(rows.size() - 1);
        }

        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        int maxCols = 0;
        for (List<T> row : rows) {
            maxCols = Math.max(maxCols, row.size());
        }

        int startCol = 0;
        int endCol = maxCols - 1;

        while (startCol <= endCol && isListColumnEmpty(rows, startCol, emptyPredicate)) {
            startCol++;
        }
        while (endCol >= startCol && isListColumnEmpty(rows, endCol, emptyPredicate)) {
            endCol--;
        }

        List<List<T>> result = new ArrayList<>();
        for (List<T> row : rows) {
            List<T> newRow = new ArrayList<>();
            for (int col = startCol; col <= endCol; col++) {
                newRow.add(col < row.size() ? row.get(col) : emptyObj);
            }
            result.add(newRow);
        }

        return result;
    }

    private static <T> boolean isListColumnEmpty(List<List<T>> rows, int col, Predicate<T> emptyPredicate) {
        for (List<T> row : rows) {
            if (col < row.size() && !emptyPredicate.test(row.get(col))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 去除边缘的空行和空列 (null 判空)
     * @param matrix 传入的二维数组
     * @return 去除边缘空行空列后的新二维数组
     */
    public static <T> T[][] stripEmptyBorders(T[][] matrix) {
        return stripEmptyBorders(matrix, Objects::isNull, null);
    }

    /**
     * 去除边缘的空行和空列
     * @param matrix 传入的二维数组
     * @param emptyPredicate 如何判断
     * @param emptyObj 生成新的矩阵时, 用于填充空位的对象
     * @return 去除边缘空行空列后的新二维数组
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] stripEmptyBorders(T[][] matrix, Predicate<T> emptyPredicate, T emptyObj) {
        if (matrix == null || matrix.length == 0) {
            if (matrix == null) {
                return (T[][]) new Object[0][];
            }
            return Arrays.copyOf(matrix, 0);
        }

        int startRow = 0;
        int endRow = matrix.length - 1;

        while (startRow <= endRow && isRowEmpty(matrix[startRow], emptyPredicate)) {
            startRow++;
        }
        while (endRow >= startRow && isRowEmpty(matrix[endRow], emptyPredicate)) {
            endRow--;
        }

        if (startRow > endRow) {
            return Arrays.copyOf(matrix, 0);
        }

        int maxCols = 0;
        for (int r = startRow; r <= endRow; r++) {
            if (matrix[r] != null) {
                maxCols = Math.max(maxCols, matrix[r].length);
            }
        }

        int startCol = 0;
        int endCol = maxCols - 1;

        while (startCol <= endCol && isArrayColumnEmpty(matrix, startRow, endRow, startCol, emptyPredicate)) {
            startCol++;
        }
        while (endCol >= startCol && isArrayColumnEmpty(matrix, startRow, endRow, endCol, emptyPredicate)) {
            endCol--;
        }

        int newRows = endRow - startRow + 1;
        int newCols = endCol - startCol + 1;
        Class<?> componentType = matrix.getClass().getComponentType().getComponentType();
        @SuppressWarnings("unchecked")
        T[][] result = (T[][]) Array.newInstance(componentType, newRows, newCols);

        for (int r = startRow; r <= endRow; r++) {
            T[] row = matrix[r];
            @SuppressWarnings("unchecked")
            T[] newRow = (T[]) Array.newInstance(componentType, newCols);
            for (int c = startCol; c <= endCol; c++) {
                newRow[c - startCol] = (row != null && c < row.length) ? row[c] : emptyObj;
            }
            result[r - startRow] = newRow;
        }

        return result;
    }

    private static <T> boolean isRowEmpty(T[] row, Predicate<T> emptyPredicate) {
        if (row == null) {
            return true;
        }
        for (T cell : row) {
            if (!emptyPredicate.test(cell)) {
                return false;
            }
        }
        return true;
    }

    private static <T> boolean isArrayColumnEmpty(T[][] matrix, int startRow, int endRow, int col, Predicate<T> emptyPredicate) {
        for (int r = startRow; r <= endRow; r++) {
            T[] row = matrix[r];
            if (row != null && col < row.length && !emptyPredicate.test(row[col])) {
                return false;
            }
        }
        return true;
    }

}
