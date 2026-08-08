package crypticlib.perm;

import crypticlib.CrypticLib;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum VelocityPermManager implements PermManager {

    INSTANCE;

    private final Map<String, PermInfo> permissions = new ConcurrentHashMap<>();

    @Override
    public VelocityPermManager regPerm(PermInfo permission) {
        if (permission == null || permission.permission() == null || permission.permission().isEmpty())
            return this;
        //permDef 为 null 时与 permission 为 null 一样静默返回,避免抛 NPE
        if (permission.permDef() == null)
            return this;
        if (permission.permDef() == PermDef.TRUE) {
            //PermDef.TRUE 权限由 VelocityPlugin#onPermissionsSetup 在 PermissionsSetupEvent 时应用
            permissions.put(permission.permission(), permission);
        } else {
            //Velocity 没有 OP 概念,OP/NOT_OP/FALSE 无法映射,仅记录 debug 日志说明该默认值不生效
            CrypticLib.debug("Velocity does not support PermDef " + permission.permDef()
                + " for permission '" + permission.permission() + "', its default value will not take effect");
        }
        return this;
    }

    public Map<String, PermInfo> permissions() {
        //返回不可变视图,防止调用方绕过 regPerm 的校验任意增删
        return Collections.unmodifiableMap(permissions);
    }

}
