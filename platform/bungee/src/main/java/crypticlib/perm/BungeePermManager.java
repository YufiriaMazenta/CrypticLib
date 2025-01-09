package crypticlib.perm;

import net.md_5.bungee.api.CommandSender;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public enum BungeePermManager implements PermManager {

    INSTANCE;
    private final Map<String, PermInfo> permissions = new ConcurrentHashMap<>();

    @Override
    public BungeePermManager regPerm(PermInfo permission) {
        if (permission == null || permission.permission() == null || permission.permission().isEmpty())
            return this;
        if (Objects.requireNonNull(permission.permDef()) == PermDef.TRUE) {
            permissions.put(permission.permission(), permission);
        }
        return this;
    }

    @Override
    public boolean hasPermission(Object sender, String permission) {
        if (sender instanceof CommandSender) {
            return ((CommandSender) sender).hasPermission(permission);
        }
        return false;
    }

    public Map<String, PermInfo> permissions() {
        return permissions;
    }

}
