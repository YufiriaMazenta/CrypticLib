package crypticlib.script.vm;

import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.Instruction;
import crypticlib.script.compile.OpCode;
import crypticlib.script.func.ScriptFunction;
import crypticlib.script.func.ScriptFunctionRegistry;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * 栈式虚拟机
 * 执行编译后的指令序列
 */
public class ScriptVM {

    private final CompiledScript script;
    private final ScriptContext context;
    private final Deque<ScriptValue> stack = new ArrayDeque<>();
    private int pc;
    private boolean returned;

    public ScriptVM(CompiledScript script, ScriptContext context) {
        this.script = script;
        this.context = context;
    }

    /**
     * 执行编译后的脚本
     * @return 执行结果（栈顶值）
     */
    public ScriptValue execute() {
        stack.clear();
        pc = 0;
        returned = false;
        List<Instruction> instructions = script.instructions();

        while (pc < instructions.size() && !returned) {
            Instruction inst = instructions.get(pc);
            pc++;
            OpCode op = inst.opCode();

            if (op == OpCode.PUSH) {
                stack.push(inst.operand());
            } else if (op == OpCode.POP) {
                stack.pop();
            } else if (op == OpCode.DUP) {
                stack.push(stack.peek());
            } else if (op == OpCode.CMP_EQ) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.compare(r) == 0));
            } else if (op == OpCode.CMP_NEQ) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.compare(r) != 0));
            } else if (op == OpCode.CMP_GT) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.compare(r) > 0));
            } else if (op == OpCode.CMP_GTE) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.compare(r) >= 0));
            } else if (op == OpCode.CMP_LT) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.compare(r) < 0));
            } else if (op == OpCode.CMP_LTE) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.compare(r) <= 0));
            } else if (op == OpCode.AND) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.asBoolean() && r.asBoolean()));
            } else if (op == OpCode.OR) {
                ScriptValue r = stack.pop(), l = stack.pop();
                stack.push(ScriptValue.of(l.asBoolean() || r.asBoolean()));
            } else if (op == OpCode.NOT) {
                stack.push(ScriptValue.of(!stack.pop().asBoolean()));
            } else if (op == OpCode.CALL) {
                executeCall(inst);
            } else if (op == OpCode.JUMP) {
                pc = inst.jumpOffset();
            } else if (op == OpCode.JUMP_IF_FALSE) {
                if (!stack.pop().asBoolean()) {
                    pc = inst.jumpOffset();
                }
            } else if (op == OpCode.RETURN) {
                returned = true;
            } else if (op == OpCode.NOP) {
                // 空操作
            }
        }

        return stack.isEmpty() ? ScriptValue.nil() : stack.peek();
    }

    /**
     * 提前返回（供 return 函数调用）
     */
    public void doReturn(ScriptValue value) {
        stack.push(value);
        returned = true;
    }

    private void executeCall(Instruction inst) {
        String funcName = inst.funcName();
        int argCount = inst.jumpOffset();

        ScriptValue[] args = new ScriptValue[argCount];
        for (int i = argCount - 1; i >= 0; i--) {
            args[i] = stack.pop();
        }

        ScriptFunction func = ScriptFunctionRegistry.INSTANCE.getFunction(funcName);
        if (func == null) {
            throw new ScriptException("Unknown function: " + funcName + " at line " + inst.line());
        }

        ScriptValue result = func.execute(context, args);
        stack.push(result == null ? ScriptValue.nil() : result);
    }
}
