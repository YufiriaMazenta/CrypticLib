package crypticlib.script.compile;

import crypticlib.script.ScriptValue;

/**
 * 编译后的指令
 */
public class Instruction {

    private final OpCode opCode;
    private final ScriptValue operand;
    private final String funcName;
    private final int jumpOffset;
    private final int line;

    public Instruction(OpCode opCode, ScriptValue operand, String funcName, int jumpOffset, int line) {
        this.opCode = opCode;
        this.operand = operand;
        this.funcName = funcName;
        this.jumpOffset = jumpOffset;
        this.line = line;
    }

    /** PUSH value */
    public static Instruction push(ScriptValue value, int line) {
        return new Instruction(OpCode.PUSH, value, null, 0, line);
    }

    /** 简单指令（无操作数） */
    public static Instruction of(OpCode opCode, int line) {
        return new Instruction(opCode, null, null, 0, line);
    }

    /** CALL funcName argCount */
    public static Instruction call(String funcName, int argCount, int line) {
        return new Instruction(OpCode.CALL, null, funcName, argCount, line);
    }

    /** JUMP / JUMP_IF_FALSE offset */
    public static Instruction jump(OpCode opCode, int offset, int line) {
        return new Instruction(opCode, null, null, offset, line);
    }

    /** LOAD_VAR varName */
    public static Instruction loadVar(String varName, int line) {
        return new Instruction(OpCode.LOAD_VAR, null, varName, 0, line);
    }

    public OpCode opCode()     { return opCode; }
    public ScriptValue operand() { return operand; }
    public String funcName()   { return funcName; }
    public int jumpOffset()    { return jumpOffset; }
    public int line()          { return line; }

    @Override
    public String toString() {
        switch (opCode) {
            case PUSH: return "PUSH " + operand;
            case CALL: return "CALL " + funcName + " " + jumpOffset;
            case LOAD_VAR: return "LOAD_VAR " + funcName;
            case JUMP:
            case JUMP_IF_FALSE: return opCode + " " + jumpOffset;
            default: return opCode.name();
        }
    }
}
