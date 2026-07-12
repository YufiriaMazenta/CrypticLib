package crypticlib.perm;

import crypticlib.Invoker;

public class PermInfo {

    public static PermManager PERM_MANAGER;
    private final String permission;
    private final PermDef permDef;

    public PermInfo(String permission) {
        this(permission, PermDef.OP);
    }

    public PermInfo(String permission, PermDef permDef) {
        this.permission = permission;
        this.permDef = permDef;
    }

    public String permission() {
        return permission;
    }

    public PermDef permDef() {
        return permDef;
    }

    public void register() {
        if (PERM_MANAGER != null) {
            PERM_MANAGER.regPerm(this);
        }
    }

    public boolean hasPermission(Invoker invoker) {
        return invoker.hasPermission(permission);
    }

}
