package crypticlib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MatrixHelper {

    public static <T> List<List<T>> removeEmptyColumnAndLine(List<List<T>> matrix) {
        // 拷贝一份外层列表,避免修改入参(内层列表不做原地修改)
        List<List<T>> rows = new ArrayList<>(matrix);

        // 删除开头的空行
        while (!rows.isEmpty() && rows.get(0).stream().allMatch(Objects::isNull)) {
            rows.remove(0);
        }
        // 删除末尾的空行
        while (!rows.isEmpty() && rows.get(rows.size() - 1).stream().allMatch(Objects::isNull)) {
            rows.remove(rows.size() - 1);
        }

        // 若已无有效行,返回空列表
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }

        // 按每行实际长度防御性处理,允许各行长度不一致
        int maxCols = 0;
        for (List<T> row : rows) {
            maxCols = Math.max(maxCols, row.size());
        }

        // 找到第一列和最后一列的索引
        final int[] startCol = {0};
        final int[] endCol = {maxCols - 1};

        // 删除开头的空列
        while (startCol[0] <= endCol[0] && isColumnEmpty(rows, startCol[0])) {
            startCol[0] ++;
        }
        // 删除末尾的空列
        while (endCol[0] >= startCol[0] && isColumnEmpty(rows, endCol[0])) {
            endCol[0] --;
        }

        // 生成新的二维列表
        List<List<T>> result = new ArrayList<>();
        for (List<T> row : rows) {
            List<T> newRow = new ArrayList<>();
            for (int col = startCol[0]; col <= endCol[0]; col++) {
                // 该行可能短于当前列,缺失的位置补null
                newRow.add(col < row.size() ? row.get(col) : null);
            }
            result.add(newRow);
        }

        return result;
    }

    /**
     * 判断某一列是否全部为空(所有行在该列均为null或该行不存在此列)
     */
    private static <T> boolean isColumnEmpty(List<List<T>> rows, int col) {
        for (List<T> row : rows) {
            if (col < row.size() && row.get(col) != null) {
                return false;
            }
        }
        return true;
    }

}
