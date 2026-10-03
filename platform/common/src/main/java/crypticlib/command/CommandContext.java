package crypticlib.command;

import crypticlib.Invoker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 命令执行上下文
 * 携带命令执行时需要的信息，替代执行链路上传递的执行者与参数
 */
public class CommandContext {

    protected final @NotNull Invoker invoker;
    protected final @NotNull List<String> args;
    /**
     * 执行路径上从根节点到当前节点之前的全部节点，首位为根节点，不含当前节点
     */
    protected final @NotNull List<CommandNode> previousNodes;
    protected final Map<String, CommandContextVariable<?>> contextVariables;

    /**
     * 创建一个根命令上下文，执行路径为空
     *
     * @param invoker 执行者
     * @param args    参数
     */
    public CommandContext(@NotNull Invoker invoker, @NotNull List<String> args) {
        this(invoker, args, new ArrayList<>(), new HashMap<>());
    }

    /**
     * 创建一个命令上下文
     *
     * @param invoker       执行者
     * @param args          当前节点的剩余参数
     * @param previousNodes 执行路径上从根节点到当前节点之前的全部节点
     */
    public CommandContext(@NotNull Invoker invoker, @NotNull List<String> args, @NotNull List<CommandNode> previousNodes, Map<String, CommandContextVariable<?>> contextVariables) {
        this.invoker = Objects.requireNonNull(invoker);
        //构造时一次性包装为不可变视图并缓存，访问器直接返回字段，避免每次调用重复包装产生开销
        this.args = Collections.unmodifiableList(new ArrayList<>(args));
        this.previousNodes = Collections.unmodifiableList(new ArrayList<>(previousNodes));
        this.contextVariables = new ConcurrentHashMap<>(contextVariables);
    }



    /**
     * 获取此上下文的执行者
     *
     * @return 此上下文的执行者
     */
    public @NotNull Invoker invoker() {
        return invoker;
    }

    /**
     * 当前节点的剩余参数
     *
     * @return 当前节点剩余参数的只读视图
     */
    public @NotNull List<String> args() {
        return args;
    }

    /**
     * 执行路径上从根节点到当前节点之前的全部节点
     *
     * @return 执行路径的只读视图
     */
    public @NotNull List<CommandNode> previousNodes() {
        return previousNodes;
    }

    /**
     * 获取上一个节点，即执行路径上当前节点的父节点
     *
     * @return 上一个节点，根节点执行时返回null
     */
    public @Nullable CommandNode previousNode() {
        return previousNodes.isEmpty() ? null : previousNodes.get(previousNodes.size() - 1);
    }

    /**
     * 派生执行子命令所需的上下文：消耗一个子命令名，并将currentNode记入执行路径
     * <p>
     * 注意：此方法要求args的首参已被确认为子命令名，即只能在首参匹配到子命令后调用，
     * 否则会因尝试消耗不存在的参数抛出IndexOutOfBoundsException
     *
     * @param currentNode 当前所处的节点
     * @return 派生出的子命令上下文
     */
    public @NotNull CommandContext next(@NotNull CommandNode currentNode) {
        List<CommandNode> path = new ArrayList<>(previousNodes);
        path.add(currentNode);
        return new CommandContext(invoker, args.subList(1, args.size()), path, contextVariables);
    }

    @SuppressWarnings("unchecked")
    public <T> void putVariable(String key, T newValue) {
        contextVariables.put(key, new CommandContextVariable<>((Class<T>) newValue.getClass(), newValue));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<CommandContextVariable<T>> getVariable(String key, Class<T> typeClass) {
        if (!contextVariables.containsKey(key)) {
            return Optional.empty();
        }
        CommandContextVariable<?> variable = contextVariables.get(key);
        if (!variable.type().equals(typeClass)) {
            return Optional.empty();
        }
        return Optional.of((CommandContextVariable<T>) variable);
    }

}
