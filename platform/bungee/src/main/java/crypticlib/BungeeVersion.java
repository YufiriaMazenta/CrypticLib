package crypticlib;

import com.google.gson.Gson;
import net.md_5.bungee.api.ProxyServer;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BungeeVersion {

    private final int version;

    private BungeeVersion(int version) {
        this.version = version;
        versionMap.put(version, this);
    }

    public int version() {
        return version;
    }


    /**
     * 判断当前的版本是否低于一个版本
     * @param version 用于比较的版本
     * @return 是否低于此版本
     */
    public boolean before(BungeeVersion version) {
        return this.version < version.version;
    }

    /**
     * 判断当前的版本是否最高是一个版本，即低于或等于此版本
     * @param version 用于比较的版本
     * @return 是否最高是此版本
     */
    public boolean beforeOrEquals(BungeeVersion version) {
        return this.version <= version.version;
    }

    /**
     * 判断当前的版本是否高于一个版本
     * @param version 用于比较的版本
     * @return 是否高于此版本
     */
    public boolean after(BungeeVersion version) {
        return this.version > version.version;
    }

    /**
     * 判断当前的版本是否最少是一个版本，即高于或等于此版本
     * @param version 用于比较的版本
     * @return 是否最少是此版本
     */
    public boolean afterOrEquals(BungeeVersion version) {
        return this.version >= version.version;
    }

    private static final Map<Integer, BungeeVersion> versionMap = new ConcurrentHashMap<>();
    private static BungeeVersion current;

    public static final BungeeVersion V1_13 = new BungeeVersion(11300);

    public static final BungeeVersion V1_14 = new BungeeVersion(11400);

    public static final BungeeVersion V1_15 = new BungeeVersion(11500);

    public static final BungeeVersion V1_16 = new BungeeVersion(11600);

    public static final BungeeVersion V1_17 = new BungeeVersion(11700);

    public static final BungeeVersion V1_18 = new BungeeVersion(11800);

    public static final BungeeVersion V1_19 = new BungeeVersion(19000);

    public static final BungeeVersion V1_20 = new BungeeVersion(12000);

    public static final BungeeVersion V1_21 = new BungeeVersion(12100);


    public static BungeeVersion current() {
        if (current == null) {
            loadCurrentVersion();
        }
        return current;
    }

    private static void loadCurrentVersion() {
        //获取游戏版本
        String versionStr = ProxyServer.getInstance().getGameVersion();
        //拿到支持的最后一个版本
        versionStr = versionStr.substring(versionStr.lastIndexOf(",") + 1);
        //将.x去掉
        versionStr = versionStr.replace(".x", "").trim();
        if (versionStr.contains("-")) {
            versionStr = versionStr.substring(0, versionStr.indexOf("-"));
        }
        String[] split = versionStr.split("\\.");
        int BungeeVersion;
        BungeeVersion = 0;
        BungeeVersion += (Integer.parseInt(split[0]) * 10000);
        BungeeVersion += (Integer.parseInt(split[1]) * 100);
        if (split.length > 2)
            BungeeVersion += Integer.parseInt(split[2]);
        if (!versionMap.containsKey(BungeeVersion)) {
            current = new BungeeVersion(BungeeVersion);
        } else {
            current = versionMap.get(BungeeVersion);
        }
    }
    
}
