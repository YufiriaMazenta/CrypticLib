package crypticlib.perm;

public interface PermManager {

    /**
     * 注册一个权限及其默认值。
     * <p>
     * 各平台对 {@link PermDef} 的支持并不一致:
     * <ul>
     *     <li>Bukkit: OP/NOT_OP/TRUE/FALSE 四种默认值全部映射为 Bukkit PermissionDefault 并生效。</li>
     *     <li>Bungee: 仅 TRUE 生效(登录时对玩家 setPermission(true)),OP/NOT_OP/FALSE 无对应概念,仅记录 debug 日志。</li>
     *     <li>Velocity: 仅 TRUE 生效(通过 PermissionsSetupEvent 应用),OP/NOT_OP/FALSE 无对应概念,仅记录 debug 日志。</li>
     * </ul>
     * 因此 PermDef.OP(单参构造的默认值)声明的权限在两个代理端不会赋予任何玩家。
     *
     * @param permission 权限信息
     * @return 当前 PermManager
     */
    PermManager regPerm(PermInfo permission);

}
