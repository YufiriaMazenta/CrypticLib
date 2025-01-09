package crypticlib.perm;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public enum BukkitPermManager implements PermManager {

    INSTANCE;

    @Override
    public BukkitPermManager regPerm(PermInfo permission) {
        if (permission == null || permission.permission() == null || permission.permission().isEmpty())
            return this;
        Permission permissionObj = Bukkit.getPluginManager().getPermission(permission.permission());
        PermissionDefault permissionDefault = PermissionDefault.valueOf(permission.permDef().name().toUpperCase());
        if (permissionObj != null) {
            permissionObj.setDefault(permissionDefault);
        } else {
            permissionObj = new Permission(permission.permission());
            permissionObj.setDefault(permissionDefault);
            Bukkit.getPluginManager().addPermission(permissionObj);
        }
        return this;
    }

    @Override
    public boolean hasPermission(Object sender, String permission) {
        if (sender instanceof Permissible) {
            return ((Permissible) sender).hasPermission(permission);
        }
        return false;
    }

}
