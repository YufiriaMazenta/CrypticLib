package crypticlib.internal.perm;

import crypticlib.listener.EventListener;
import crypticlib.perm.BungeePermManager;
import crypticlib.perm.PermDef;
import crypticlib.perm.PermInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Map;

@EventListener
public enum BungeePermissionHandler implements Listener {

    INSTANCE;

    @EventHandler
    public void onPlayerLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        Map<String, PermInfo> permissions = BungeePermManager.INSTANCE.permissions();
        permissions.forEach(
            (permission, permInfo) -> {
                if (PermDef.TRUE.equals(permInfo.permDef())) {
                    player.setPermission(permission, true);
                }
            }
        );
    }

}
