package crypticlib.action.condition;

import crypticlib.util.IOHelper;
import crypticlib.util.StringHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析Condition字符串,支持格式"a运算符b"
 * 运算符一共有==,>=,<=,>,<,=几种
 * 比较内容会取运算符两侧所有字符,包括空格
 * 当左右都为数字时,会进行大小比较
 * 当一侧不为数字时,>会检测左侧是否包含右侧且长度大于右侧,而>=只会检测左侧是否包含右侧,其他运算符以此类推
 */
public class CommonConditionComparator {

    // 支持的运算符列表（按长度降序排列）
    private static final List<String> OPERATORS = Arrays.asList("==", ">=", "<=", ">", "<", "=");

    // 正则表达式模式（预编译）
    private static final Pattern CONDITION_PATTERN;

    static {
        // 按运算符长度降序生成正则表达式
        String operatorRegex = OPERATORS.stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .map(Pattern::quote)
            .reduce((a, b) -> a + "|" + b)
            .orElseThrow(() -> new NoSuchElementException("No value present"));

        // 正则表达式结构：左操作数 运算符 右操作数（允许任意字符，包括空格）
        String regex = "^(.+?)(" + operatorRegex + ")(.+)$";
        CONDITION_PATTERN = Pattern.compile(regex);
    }

    /**
     * 解析并比较表达式
     * @param condition 输入表达式（如 "a == b"）
     * @return 比较结果
     * @throws IllegalArgumentException 格式错误时抛出异常
     */
    public static boolean compare(String condition) {
        ParsedCondition parsed = parseCondition(condition);
        String left = parsed.left();
        String operator = parsed.operator();
        String right = parsed.right();
        IOHelper.debug("Comparing condition: left=" + left + ", operator=" + operator + ", right=" + right);

        // 判断是否为数值类型
        boolean isLeftNumber = StringHelper.isNumber(left);
        boolean isRightNumber = StringHelper.isNumber(right);

        // 根据运算符执行比较
        switch (operator) {
            case "==":
            case "=":
                return handleEquals(left, right, isLeftNumber && isRightNumber);
            case ">":
                return handleGreaterThan(left, right, isLeftNumber && isRightNumber);
            case ">=":
                return handleGreaterThanOrEqual(left, right, isLeftNumber && isRightNumber);
            case "<":
                return handleLessThan(left, right, isLeftNumber && isRightNumber);
            case "<=":
                return handleLessThanOrEqual(left, right, isLeftNumber && isRightNumber);
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    // 解析表达式为左操作数、运算符、右操作数
    private static ParsedCondition parseCondition(String condition) {
        Matcher matcher = CONDITION_PATTERN.matcher(condition);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid condition format: " + condition);
        }
        return new ParsedCondition(matcher.group(1), matcher.group(2), matcher.group(3));
    }

    // 处理 == 运算符
    private static boolean handleEquals(String left, String right, boolean bothNumbers) {
        return bothNumbers ?
            Double.parseDouble(left) == Double.parseDouble(right) :
            left.equals(right);
    }

    // 处理 > 运算符
    private static boolean handleGreaterThan(String left, String right, boolean bothNumbers) {
        return bothNumbers ?
            Double.parseDouble(left) > Double.parseDouble(right) :
            left.contains(right) && left.length() > right.length();
    }

    // 处理 >= 运算符
    private static boolean handleGreaterThanOrEqual(String left, String right, boolean bothNumbers) {
        return bothNumbers ?
            Double.parseDouble(left) >= Double.parseDouble(right) :
            left.contains(right);
    }

    // 处理 < 运算符
    private static boolean handleLessThan(String left, String right, boolean bothNumbers) {
        return bothNumbers ?
            Double.parseDouble(left) < Double.parseDouble(right) :
            right.contains(left) && right.length() > left.length();
    }

    // 处理 <= 运算符
    private static boolean handleLessThanOrEqual(String left, String right, boolean bothNumbers) {
        return bothNumbers ?
            Double.parseDouble(left) <= Double.parseDouble(right) :
            right.contains(left);
    }

    private static class ParsedCondition {

        private final String left;
        private final String operator;
        private final String right;

        public ParsedCondition(String left, String operator, String right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public String left() {
            return left;
        }

        public String operator() {
            return operator;
        }

        public String right() {
            return right;
        }

    }

}
