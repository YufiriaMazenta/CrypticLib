package crypticlib.script;

/**
 * 字符串插值部分的接口
 * 用于表示插值字符串中的文本片段或变量引用
 *
 * 例如 "Hello ${name}!" 包含：
 * - Text("Hello ")
 * - Variable("name")
 * - Text("!")
 */
public interface InterpolationPart {

    /**
     * 文本片段
     */
    final class Text implements InterpolationPart {
        private final String text;

        public Text(String text) {
            this.text = text;
        }

        public String text() { return text; }

        @Override
        public String toString() { return text; }
    }

    /**
     * 变量引用片段
     */
    final class Variable implements InterpolationPart {
        private final String variableName;

        public Variable(String variableName) {
            this.variableName = variableName;
        }

        public String variableName() { return variableName; }

        @Override
        public String toString() { return "${" + variableName + "}"; }
    }
}
