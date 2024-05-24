package crypticlib.api.command;

import crypticlib.api.permission.PermissionDefault;
import org.jetbrains.annotations.Nullable;

public class CommandInfo {

    private String name;
    private @Nullable String permission;
    private @Nullable PermissionDefault permissionDefault;
    private @Nullable String[] aliases;

    public CommandInfo(String name, @Nullable String permission, @Nullable PermissionDefault permissionDefault, @Nullable String[] aliases) {
        this.name = name;
        this.permission = permission;
        this.permissionDefault = permissionDefault;
        this.aliases = aliases;
    }

    public String getName() {
        return name;
    }

    public CommandInfo setName(String name) {
        this.name = name;
        return this;
    }

    public @Nullable String getPermission() {
        return permission;
    }

    public CommandInfo setPermission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    public @Nullable PermissionDefault getPermissionDefault() {
        return permissionDefault;
    }

    public CommandInfo setPermissionDefault(@Nullable PermissionDefault permissionDefault) {
        this.permissionDefault = permissionDefault;
        return this;
    }

    public @Nullable String[] getAliases() {
        return aliases;
    }

    public @Nullable CommandInfo setAliases(String[] aliases) {
        this.aliases = aliases;
        return this;
    }
}
