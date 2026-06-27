package crypticlib.script.compile;

import crypticlib.script.ScriptValue;
import crypticlib.script.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 脚本编译器
 * 将 AST 编译为栈式虚拟机的指令序列
 */
public class ScriptCompiler {

    private final List<Instruction> instructions = new ArrayList<>();

    /**
     * 编译 AST 为指令序列
     * @param name 脚本名称（用于调试）
     * @param node AST 根节点
     * @return 编译后的脚本
     */
    public CompiledScript compile(String name, ASTNode node) {
        instructions.clear();
        emitNode(node);
        instructions.add(Instruction.of(OpCode.RETURN, 0));
        return new CompiledScript(name, new ArrayList<>(instructions));
    }

    private void emitNode(ASTNode node) {
        if (node instanceof ASTNode.LiteralNode) {
            emitLiteral((ASTNode.LiteralNode) node);
        } else if (node instanceof ASTNode.IdentifierNode) {
            emitIdentifier((ASTNode.IdentifierNode) node);
        } else if (node instanceof ASTNode.BinaryOpNode) {
            emitBinaryOp((ASTNode.BinaryOpNode) node);
        } else if (node instanceof ASTNode.UnaryOpNode) {
            emitUnaryOp((ASTNode.UnaryOpNode) node);
        } else if (node instanceof ASTNode.FunctionCallNode) {
            emitFunctionCall((ASTNode.FunctionCallNode) node);
        } else if (node instanceof ASTNode.IfNode) {
            emitIf((ASTNode.IfNode) node);
        } else if (node instanceof ASTNode.ReturnNode) {
            emitReturn((ASTNode.ReturnNode) node);
        } else if (node instanceof ASTNode.BlockNode) {
            emitBlock((ASTNode.BlockNode) node);
        }
    }

    private void emitLiteral(ASTNode.LiteralNode node) {
        Object val = node.value();
        ScriptValue value;
        if (val instanceof String) {
            value = ScriptValue.of((String) val);
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

    private void emitIdentifier(ASTNode.IdentifierNode node) {
        // 标识符作为无参函数调用
        instructions.add(Instruction.call(node.name(), 0, node.line()));
    }

    private void emitBinaryOp(ASTNode.BinaryOpNode node) {
        if ("&&".equals(node.operator())) {
            // 短路求值: left 为 false 时直接返回 false，不计算 right
            emitNode(node.left());
            instructions.add(Instruction.of(OpCode.DUP, node.line()));
            int jumpIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP_IF_FALSE, 0, node.line()));
            instructions.add(Instruction.of(OpCode.POP, node.line()));
            emitNode(node.right());
            patchJump(jumpIdx);
            return;
        }

        if ("||".equals(node.operator())) {
            // 短路求值: left 为 true 时直接返回 true，不计算 right
            emitNode(node.left());
            instructions.add(Instruction.of(OpCode.DUP, node.line()));
            int jumpIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP_IF_FALSE, 0, node.line()));
            int skipRightIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP, 0, node.line()));

            patchJump(jumpIdx);
            instructions.add(Instruction.of(OpCode.POP, node.line()));
            emitNode(node.right());

            patchJump(skipRightIdx);
            return;
        }

        // 其他运算符
        emitNode(node.left());
        emitNode(node.right());
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
        } else {
            throw new IllegalArgumentException("Unknown operator: " + op);
        }
        instructions.add(Instruction.of(opCode, node.line()));
    }

    private void emitUnaryOp(ASTNode.UnaryOpNode node) {
        emitNode(node.operand());
        if ("!".equals(node.operator())) {
            instructions.add(Instruction.of(OpCode.NOT, node.line()));
        }
    }

    private void emitFunctionCall(ASTNode.FunctionCallNode node) {
        for (ASTNode arg : node.args()) {
            emitNode(arg);
        }
        instructions.add(Instruction.call(node.name(), node.args().size(), node.line()));
    }

    private void emitIf(ASTNode.IfNode node) {
        emitNode(node.condition());

        int jumpToElseIdx = instructions.size();
        instructions.add(Instruction.jump(OpCode.JUMP_IF_FALSE, 0, node.line()));

        for (ASTNode stmt : node.thenBody()) {
            emitNode(stmt);
        }

        if (!node.elseBody().isEmpty()) {
            int jumpOverElseIdx = instructions.size();
            instructions.add(Instruction.jump(OpCode.JUMP, 0, node.line()));

            patchJump(jumpToElseIdx);

            for (ASTNode stmt : node.elseBody()) {
                emitNode(stmt);
            }

            patchJump(jumpOverElseIdx);
        } else {
            patchJump(jumpToElseIdx);
        }
    }

    private void emitBlock(ASTNode.BlockNode node) {
        for (ASTNode stmt : node.statements()) {
            emitNode(stmt);
        }
    }

    private void emitReturn(ASTNode.ReturnNode node) {
        if (node.value() != null) {
            emitNode(node.value());
        } else {
            instructions.add(Instruction.push(ScriptValue.nil(), node.line()));
        }
        instructions.add(Instruction.of(OpCode.RETURN, node.line()));
    }

    private void patchJump(int instructionIndex) {
        Instruction old = instructions.get(instructionIndex);
        instructions.set(instructionIndex, Instruction.jump(old.opCode(), instructions.size(), old.line()));
    }
}
