package crypticlib.perm;

import com.velocitypowered.api.command.CommandSource;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public enum VelocityPermManager implements PermManager {

    INSTANCE;

    private final Map<String, PermInfo> permissions = new ConcurrentHashMap<>();

    @Override
    public VelocityPermManager regPerm(PermInfo permission) {
        if (permission == null || permission.permission() == null || permission.permission().isEmpty())
            return this;
        if (Objects.requireNonNull(permission.permDef()) == PermDef.TRUE) {
            permissions.put(permission.permission(), permission);
        }
        return this;
    }

    public Map<String, PermInfo> permissions() {
        return permissions;
    }

}
