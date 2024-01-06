package crypticlib.perm;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public enum PermissionManager {

    INSTANCE;

    public PermissionManager regPerm(String permission, @NotNull PermDef permDef) {
        if (permission == null || permission.isEmpty())
            return this;
        Permission permissionObj = Bukkit.getPluginManager().getPermission(permission);
        PermissionDefault permissionDefault = PermissionDefault.valueOf(permDef.name().toUpperCase());
        if (permissionObj != null) {
            permissionObj.setDefault(permissionDefault);
        } else {
            permissionObj = new Permission(permission);
            permissionObj.setDefault(permissionDefault);
            Bukkit.getPluginManager().addPermission(permissionObj);
        }
        return this;
    }

}
