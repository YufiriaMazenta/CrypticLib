package crypticlib.perm;

public class PermInfo {

    private String permission;
    private PermDef permDef;

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

    public PermInfo setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public PermDef permDef() {
        return permDef;
    }

    public PermInfo setPermDef(PermDef permDef) {
        this.permDef = permDef;
        return this;
    }

    public void register() {
        PermManager.INSTANCE.regPerm(this);
    }

}
