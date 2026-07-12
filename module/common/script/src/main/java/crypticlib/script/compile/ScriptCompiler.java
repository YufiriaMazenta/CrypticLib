package crypticlib.script.compile;

import crypticlib.script.InterpolationPart;
import crypticlib.script.ScriptValue;
import crypticlib.script.ast.ASTNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 脚本编译器
 * 将 AST 编译为栈式虚拟机的指令序列
 */
public class ScriptCompiler {

    /**
     * 编译 AST 为指令序列
     * @param name 脚本名称（用于调试）
     * @param node AST 根节点
     * @return 编译后的脚本
     */
    public CompiledScript compile(String name, ASTNode node) {
        List<Instruction> instructions = new ArrayList<>();
        emitNode(node, instructions);
        instructions.add(Instruction.of(OpCode.RETURN, 0));
        return new CompiledScript(name, instructions);
    }

    private void emitNode(ASTNode node, List<Instruction> instructions) {
        if (node instanceof ASTNode.LiteralNode) {
            emitLiteral((ASTNode.LiteralNode) node, instructions);
        } else if (node instanceof ASTNode.IdentifierNode) {
            emitIdentifier((ASTNode.IdentifierNode) node, instructions);
        } else if (node instanceof ASTNode.VariableReferenceNode) {
            emitVariableReference((ASTNode.VariableReferenceNode) node, instructions);
        } else if (node instanceof ASTNode.StringInterpolationNode) {
            emitStringInterpolation((ASTNode.StringInterpolationNode) node, instructions);
        } else if (node instanceof ASTNode.BinaryOpNode) {
            emitBinaryOp((ASTNode.BinaryOpNode) node, instructions);
        } else if (node instanceof ASTNode.UnaryOpNode) {
            emitUnaryOp((ASTNode.UnaryOpNode) node, instructions);
        } else if (node instanceof ASTNode.FunctionCallNode) {
            emitFunctionCall((ASTNode.FunctionCallNode) node, instructions);
        } else if (node instanceof ASTNode.IfNode) {
            emitIf((ASTNode.IfNode) node, instructions);
        } else if (node instanceof ASTNode.ReturnNode) {
            emitReturn((ASTNode.ReturnNode) node, instructions);
        } else if (node instanceof ASTNode.BlockNode) {
            emitBlock((ASTNode.BlockNode) node, instructions);
        }
    }

    private void emitLiteral(ASTNode.LiteralNode node, List<Instruction> instructions) {
        Object val = node.value();
        ScriptValue value;
        if (val instanceof String) {
            value = ScriptValue.of((String) val);
        } else if (val instanceof Long) {
            value = ScriptValue.of((Long) val);
        } else if (val instanceof Integer) {
            value = ScriptValue.of((Integer) val);
        } else if (val instanceof BigDecimal) {
            value = ScriptValue.of((BigDecimal) val);
        } else if (val instanceof Double) {
            value = ScriptValue.of((Double) val);
        } else if (val instanceof Boolean) {
            value = ScriptValue.of((Boolean) val);
        } else if (val instanceof Number) {
            value = ScriptValue.of(((Number) val).doubleValue());
        } else {
            value = ScriptValue.nil();
        }
        instructions.add(Instruction.push(value, node.line()));
    }

    private void emitIdentifier(ASTNode.IdentifierNode node, List<Instruction> instructions) {
        // 标识符作为无参函数调用
        instructions.add(Instruction.call(node.name(), 0, node.line()));
    }

    private void emitVariableReference(ASTNode.VariableReferenceNode node, List<Instruction> instructions) {
        instructions.add(Instruction.loadVar(node.variableName(), node.line()));
    }

    private void emitStringInterpolation(ASTNode.StringInterpolationNode node, List<Instruction> instructions) {
        List<InterpolationPart> parts = node.parts();
        if (parts.isEmpty()) {
            instructions.add(Instruction.push(ScriptValue.of(""), node.line()));
            return;
        }

        // 编译第一个部分
        emitInterpolationPart(parts.get(0), instructions);

        // 编译后续部分并拼接（使用 ADD 操作码，当一侧为字符串时自动拼接）
        for (int i = 1; i < parts.size(); i++) {
            emitInterpolationPart(parts.get(i), instructions);
            instructions.add(Instruction.of(OpCode.ADD, node.line()));
        }
    }

    private void emitInterpolationPart(InterpolationPart part, List<Instruction> instructions) {
        if (part instanceof InterpolationPart.Text) {
            instructions.add(Instruction.push(ScriptValue.of(((InterpolationPart.Text) part).text()), 0));
        } else if (part instanceof InterpolationPart.Variable) {
            instructions.add(Instruction.loadVar(((InterpolationPart.Variable) part).variableName(), 0));
        }
    }

    private void emitBinaryOp(ASTNode.BinaryOpNode node, List<Instruction> instructions) {
        if ("&&".equals(node.operator())) {
            // 短路求值: left 为 false 时直接返回 false，不计算 right
            emitNode(node.left(), instructions);
            instructions.add(Instruction.of(OpCode.DUP, node.line()));
            int jumpIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP_IF_FALSE, 0, node.line()));
            instructions.add(Instruction.of(OpCode.POP, node.line()));
            emitNode(node.right(), instructions);
            patchJump(jumpIdx, instructions);
            return;
        }

        if ("||".equals(node.operator())) {
            // 短路求值: left 为 true 时直接返回 true，不计算 right
            emitNode(node.left(), instructions);
            instructions.add(Instruction.of(OpCode.DUP, node.line()));
            int jumpIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP_IF_FALSE, 0, node.line()));
            int skipRightIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP, 0, node.line()));

            patchJump(jumpIdx, instructions);
            instructions.add(Instruction.of(OpCode.POP, node.line()));
            emitNode(node.right(), instructions);

            patchJump(skipRightIdx, instructions);
            return;
        }

        // 其他运算符
        emitNode(node.left(), instructions);
        emitNode(node.right(), instructions);
        String op = node.operator();
        OpCode opCode;
        if ("==".equals(op)) {
            opCode = OpCode.CMP_EQ;
        } else if ("!=".equals(op)) {
            opCode = OpCode.CMP_NEQ;
        } else if (">".equals(op)) {
            opCode = OpCode.CMP_GT;
        } else if (">=".equals(op)) {
            opCode = OpCode.CMP_GTE;
        } else if ("<".equals(op)) {
            opCode = OpCode.CMP_LT;
        } else if ("<=".equals(op)) {
            opCode = OpCode.CMP_LTE;
        } else if ("+".equals(op)) {
            opCode = OpCode.ADD;
        } else if ("-".equals(op)) {
            opCode = OpCode.SUB;
        } else if ("*".equals(op)) {
            opCode = OpCode.MUL;
        } else if ("/".equals(op)) {
            opCode = OpCode.DIV;
        } else if ("%".equals(op)) {
            opCode = OpCode.MOD;
        } else {
            throw new IllegalArgumentException("Unknown operator: " + op);
        }
        instructions.add(Instruction.of(opCode, node.line()));
    }

    private void emitUnaryOp(ASTNode.UnaryOpNode node, List<Instruction> instructions) {
        emitNode(node.operand(), instructions);
        if ("!".equals(node.operator())) {
            instructions.add(Instruction.of(OpCode.NOT, node.line()));
        } else if ("-".equals(node.operator())) {
            instructions.add(Instruction.of(OpCode.NEG, node.line()));
        }
    }

    private void emitFunctionCall(ASTNode.FunctionCallNode node, List<Instruction> instructions) {
        for (ASTNode arg : node.args()) {
            emitNode(arg, instructions);
        }
        instructions.add(Instruction.call(node.name(), node.args().size(), node.line()));
    }

    private void emitIf(ASTNode.IfNode node, List<Instruction> instructions) {
        emitNode(node.condition(), instructions);

        int jumpToElseIdx = instructions.size();
        instructions.add(Instruction.jump(OpCode.JUMP_IF_FALSE, 0, node.line()));

        for (ASTNode stmt : node.thenBody()) {
            emitNode(stmt, instructions);
        }

        if (!node.elseBody().isEmpty()) {
            int jumpOverElseIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP, 0, node.line()));

            patchJump(jumpToElseIdx, instructions);

            for (ASTNode stmt : node.elseBody()) {
                emitNode(stmt, instructions);
            }

            patchJump(jumpOverElseIdx, instructions);
        } else {
            patchJump(jumpToElseIdx, instructions);
        }
    }

    private void emitBlock(ASTNode.BlockNode node, List<Instruction> instructions) {
        for (ASTNode stmt : node.statements()) {
            emitNode(stmt, instructions);
        }
    }

    private void emitReturn(ASTNode.ReturnNode node, List<Instruction> instructions) {
        if (node.value() != null) {
            emitNode(node.value(), instructions);
        } else {
            instructions.add(Instruction.push(ScriptValue.nil(), node.line()));
        }
        instructions.add(Instruction.of(OpCode.RETURN, node.line()));
    }

    private void patchJump(int instructionIndex, List<Instruction> instructions) {
        Instruction old = instructions.get(instructionIndex);
        instructions.set(instructionIndex, Instruction.jump(old.opCode(), instructions.size(), old.line()));
    }
}
