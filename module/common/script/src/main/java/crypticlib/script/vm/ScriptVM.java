package crypticlib.script.vm;

import crypticlib.CrypticLib;
import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;
import crypticlib.script.compile.CompiledScript;
import crypticlib.script.compile.Instruction;
import crypticlib.script.compile.OpCode;
import crypticlib.script.func.ScriptFunction;
import crypticlib.script.func.ScriptFunctionRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * 栈式虚拟机
 * 执行编译后的指令序列
 */
public class ScriptVM {

    /** 默认最大指令执行次数，防止无限循环 */
    public static final int DEFAULT_MAX_INSTRUCTIONS = 100_000;

    private final CompiledScript script;
    private final ScriptContext context;
    private final int maxInstructions;
    private final Deque<ScriptValue> stack = new ArrayDeque<>();
    private int pc;
    private boolean returned;
    private boolean paused;

    public ScriptVM(CompiledScript script, ScriptContext context) {
        this(script, context, DEFAULT_MAX_INSTRUCTIONS);
    }

    public ScriptVM(CompiledScript script, ScriptContext context, int maxInstructions) {
        this.script = script;
        this.context = context;
        this.maxInstructions = maxInstructions;
    }

    /**
     * 执行编译后的脚本
     * @return 执行结果（栈顶值）
     */
    public ScriptValue execute() {
        stack.clear();
        pc = 0;
        returned = false;
        paused = false;
        return runLoop();
    }

    /**
     * 从当前状态恢复执行（用于 delay 后续恢复）
     * @return 执行结果（栈顶值）
     */
    public ScriptValue resume() {
        returned = false;
        paused = false;
        return runLoop();
    }

    /**
     * 核心执行循环
     */
    private ScriptValue runLoop() {
        List<Instruction> instructions = script.instructions();

        int executedCount = 0;
        while (pc < instructions.size() && !returned && !paused) {
            if (++executedCount > maxInstructions) {
                throw new ScriptException("Script execution exceeded maximum instruction limit (" + maxInstructions + "): " + script.sourceName());
            }

            Instruction inst = instructions.get(pc);
            pc++;
            OpCode op = inst.opCode();

            switch (op) {
                case PUSH:
                    stack.push(inst.operand());
                    break;
                case POP:
                    popStack("POP");
                    break;
                case DUP: {
                    ScriptValue top = peekStack("DUP");
                    stack.push(top);
                    break;
                }
                case CMP_EQ: {
                    ScriptValue r = popStack("CMP_EQ");
                    ScriptValue l = popStack("CMP_EQ");
                    stack.push(ScriptValue.of(l.compare(r) == 0));
                    break;
                }
                case CMP_NEQ: {
                    ScriptValue r = popStack("CMP_NEQ");
                    ScriptValue l = popStack("CMP_NEQ");
                    stack.push(ScriptValue.of(l.compare(r) != 0));
                    break;
                }
                case CMP_GT: {
                    ScriptValue r = popStack("CMP_GT");
                    ScriptValue l = popStack("CMP_GT");
                    stack.push(ScriptValue.of(l.compare(r) > 0));
                    break;
                }
                case CMP_GTE: {
                    ScriptValue r = popStack("CMP_GTE");
                    ScriptValue l = popStack("CMP_GTE");
                    stack.push(ScriptValue.of(l.compare(r) >= 0));
                    break;
                }
                case CMP_LT: {
                    ScriptValue r = popStack("CMP_LT");
                    ScriptValue l = popStack("CMP_LT");
                    stack.push(ScriptValue.of(l.compare(r) < 0));
                    break;
                }
                case CMP_LTE: {
                    ScriptValue r = popStack("CMP_LTE");
                    ScriptValue l = popStack("CMP_LTE");
                    stack.push(ScriptValue.of(l.compare(r) <= 0));
                    break;
                }
                case AND: {
                    ScriptValue r = popStack("AND");
                    ScriptValue l = popStack("AND");
                    stack.push(ScriptValue.of(l.asBoolean() && r.asBoolean()));
                    break;
                }
                case OR: {
                    ScriptValue r = popStack("OR");
                    ScriptValue l = popStack("OR");
                    stack.push(ScriptValue.of(l.asBoolean() || r.asBoolean()));
                    break;
                }
                case NOT: {
                    stack.push(ScriptValue.of(!popStack("NOT").asBoolean()));
                    break;
                }
                case ADD: {
                    ScriptValue r = popStack("ADD");
                    ScriptValue l = popStack("ADD");
                    // 任一侧为字符串时做字符串拼接
                    if (l.isString() || r.isString()) {
                        stack.push(ScriptValue.of(l.asString() + r.asString()));
                    } else if (l.isInteger() && r.isInteger()) {
                        // 两个整数使用整数运算
                        stack.push(ScriptValue.of(l.asLong() + r.asLong()));
                    } else {
                        BigDecimal result = l.asBigDecimal().add(r.asBigDecimal());
                        stack.push(ScriptValue.of(result));
                    }
                    break;
                }
                case SUB: {
                    ScriptValue r = popStack("SUB");
                    ScriptValue l = popStack("SUB");
                    if (l.isInteger() && r.isInteger()) {
                        stack.push(ScriptValue.of(l.asLong() - r.asLong()));
                    } else {
                        BigDecimal result = l.asBigDecimal().subtract(r.asBigDecimal());
                        stack.push(ScriptValue.of(result));
                    }
                    break;
                }
                case MUL: {
                    ScriptValue r = popStack("MUL");
                    ScriptValue l = popStack("MUL");
                    if (l.isInteger() && r.isInteger()) {
                        stack.push(ScriptValue.of(l.asLong() * r.asLong()));
                    } else {
                        BigDecimal result = l.asBigDecimal().multiply(r.asBigDecimal());
                        stack.push(ScriptValue.of(result));
                    }
                    break;
                }
                case DIV: {
                    ScriptValue r = popStack("DIV");
                    ScriptValue l = popStack("DIV");
                    if (l.isInteger() && r.isInteger()) {
                        long divisor = r.asLong();
                        if (divisor == 0) {
                            throw new ScriptException("Division by zero at line " + inst.line() + " in script: " + script.sourceName());
                        }
                        stack.push(ScriptValue.of(l.asLong() / divisor));
                    } else {
                        BigDecimal divisor = r.asBigDecimal();
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw new ScriptException("Division by zero at line " + inst.line() + " in script: " + script.sourceName());
                        }
                        BigDecimal result = l.asBigDecimal().divide(divisor, ScriptValue.DIV_SCALE, RoundingMode.HALF_UP);
                        stack.push(ScriptValue.of(result));
                    }
                    break;
                }
                case MOD: {
                    ScriptValue r = popStack("MOD");
                    ScriptValue l = popStack("MOD");
                    if (l.isInteger() && r.isInteger()) {
                        long divisor = r.asLong();
                        if (divisor == 0) {
                            throw new ScriptException("Modulo by zero at line " + inst.line() + " in script: " + script.sourceName());
                        }
                        stack.push(ScriptValue.of(l.asLong() % divisor));
                    } else {
                        BigDecimal divisor = r.asBigDecimal();
                        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
                            throw new ScriptException("Modulo by zero at line " + inst.line() + " in script: " + script.sourceName());
                        }
                        BigDecimal result = l.asBigDecimal().remainder(divisor);
                        stack.push(ScriptValue.of(result));
                    }
                    break;
                }
                case NEG: {
                    ScriptValue operand = popStack("NEG");
                    if (operand.isInteger()) {
                        stack.push(ScriptValue.of(-operand.asLong()));
                    } else {
                        stack.push(ScriptValue.of(operand.asBigDecimal().negate()));
                    }
                    break;
                }
                case LOAD_VAR: {
                    String varName = inst.funcName();
                    ScriptValue value = context.getVariable(varName);
                    stack.push(value != null ? value : ScriptValue.nil());
                    break;
                }
                case CALL:
                    executeCall(inst);
                    break;
                case JUMP:
                    pc = inst.jumpOffset();
                    break;
                case JUMP_IF_FALSE: {
                    if (!popStack("JUMP_IF_FALSE").asBoolean()) {
                        pc = inst.jumpOffset();
                    }
                    break;
                }
                case RETURN:
                    returned = true;
                    break;
                case NOP:
                default:
                    break;
            }
        }

        return stack.isEmpty() ? ScriptValue.nil() : stack.peek();
    }

    private ScriptValue popStack(String opName) {
        if (stack.isEmpty()) {
            throw new ScriptException("Stack underflow at operation " + opName + " (line " + (pc > 0 ? pc - 1 : 0) + ") in script: " + script.sourceName());
        }
        return stack.pop();
    }

    private ScriptValue peekStack(String opName) {
        if (stack.isEmpty()) {
            throw new ScriptException("Stack underflow at operation " + opName + " (line " + (pc > 0 ? pc - 1 : 0) + ") in script: " + script.sourceName());
        }
        return stack.peek();
    }

    /**
     * 中断执行并调度延迟恢复
     * @param delayTicks 延迟 tick 数
     */
    public void pauseAndScheduleResume(long delayTicks) {
        paused = true;
        CrypticLib.scheduler().syncLater(this::resume, delayTicks);
    }

    /**
     * 暂停脚本执行，由插件自主控制恢复时机
     * <p>
     * 调用后脚本会在当前指令执行完毕后停止，
     * 插件可在合适的时机调用 {@link #resume()} 恢复执行。
     * 与 {@link #pauseAndScheduleResume(long)} 不同，
     * 此方法不会自动调度恢复任务。
     */
    public void pause() {
        paused = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public Deque<ScriptValue> stack() {
        return stack;
    }

    public ScriptContext context() {
        return context;
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

        ScriptValue result = func.execute(context, this, args);
        if (!paused) {
            stack.push(result == null ? ScriptValue.nil() : result);
        }
    }

}
