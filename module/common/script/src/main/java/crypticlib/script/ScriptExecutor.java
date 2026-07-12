package crypticlib.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class ScriptExecutor {

    private final @Nullable UUID executorId;
    private final @NotNull ExecutorType executorType;

    public ScriptExecutor(@Nullable UUID executorId, @NotNull ExecutorType executorType) {
        this.executorId = executorId;
        this.executorType = executorType;
    }

    public Optional<UUID> executorId() {
        return Optional.ofNullable(executorId);
    }

    public @NotNull ExecutorType executorType() {
        return executorType;
    }

    public <T> Optional<T> resolvePlatformExecutor(BiFunction<@Nullable UUID, @NotNull ExecutorType, T> playerGetter) {
        return Optional.ofNullable(playerGetter.apply(executorId, executorType));
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ScriptExecutor)) return false;
        ScriptExecutor that = (ScriptExecutor) object;
        return Objects.equals(executorId, that.executorId) && executorType == that.executorType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(executorId, executorType);
    }

    public enum ExecutorType {

        PLAYER,
        CONSOLE,
        ENTITY

    }

}
