package crypticlib;

import net.md_5.bungee.api.ProxyServer;

public enum BungeeVersion {

    CURRENT(getCurrentVersion()),
    V1_13(11300),
    V1_14(11400),
    V1_15(11500),
    V1_16(11600),
    V1_17(11700),
    V1_18(11800),
    V1_19(11900),
    V1_20(12000),
    V1_21(12100);

    private final int version;

    BungeeVersion(int version) {
        this.version = version;
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
     * 判断当前的版本是否不高于一个版本，即低于或等于此版本
     * @param version 用于比较的版本
     * @return 是否不高于此版本
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
     * 判断当前的版本是否不低于一个版本，即高于或等于此版本
     * @param version 用于比较的版本
     * @return 是否不低于此版本
     */
    public boolean afterOrEquals(BungeeVersion version) {
        return this.version >= version.version;
    }

    /**
     * 判断当前的版本是否处于一个版本,即等于此版本
     * @param version 用于比较的版本
     * @return 是否处于一个版本
     */
    public boolean equals(BungeeVersion version) {
        return this.version == version.version;
    }

    public static BungeeVersion current() {
        return CURRENT;
    }

    private static int getCurrentVersion() {
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
        int bungeeVersion;
        bungeeVersion = 0;
        bungeeVersion += (Integer.parseInt(split[0]) * 10000);
        bungeeVersion += (Integer.parseInt(split[1]) * 100);
        if (split.length > 2)
            bungeeVersion += Integer.parseInt(split[2]);
        return bungeeVersion;
    }
    
}
