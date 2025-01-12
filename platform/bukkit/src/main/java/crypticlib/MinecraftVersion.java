package crypticlib;

import org.bukkit.Bukkit;

public enum MinecraftVersion {

    CURRENT(getCurrentVersion()),

    V1_13(11300),
    V1_13_1(11301),
    V1_13_2(11302),

    V1_14(11400),
    V1_14_1(11401),
    V1_14_2(11402),
    V1_14_3(11403),
    V1_14_4(11404),

    V1_15(11500),
    V1_15_1(11501),
    V1_15_2(11502),

    V1_16(11600),
    V1_16_1(11601),
    V1_16_2(11602),
    V1_16_3(11603),
    V1_16_4(11604),
    V1_16_5(11605),

    V1_17(11700),
    V1_17_1(11701),

    V1_18(11800),
    V1_18_1(11801),
    V1_18_2(11802),

    V1_19(11900),
    V1_19_1(11901),
    V1_19_2(11902),
    V1_19_3(11903),
    V1_19_4(11904),

    V1_20(12000),
    V1_20_1(12001),
    V1_20_2(12002),
    V1_20_3(12003),
    V1_20_4(12004),
    V1_20_5(12005),
    V1_20_6(12006),

    V1_21(12100),
    V1_21_1(12101),
    V1_21_2(12102),
    V1_21_3(12103),
    V1_21_4(12104);

    private final int version;

    MinecraftVersion(int version) {
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
    public boolean before(MinecraftVersion version) {
        return this.version < version.version;
    }

    /**
     * 判断当前的版本是否不高于一个版本，即低于或等于此版本
     * @param version 用于比较的版本
     * @return 是否不高于此版本
     */
    public boolean beforeOrEquals(MinecraftVersion version) {
        return this.version <= version.version;
    }

    /**
     * 判断当前的版本是否高于一个版本
     * @param version 用于比较的版本
     * @return 是否高于此版本
     */
    public boolean after(MinecraftVersion version) {
        return this.version > version.version;
    }

    /**
     * 判断当前的版本是否不低于一个版本，即高于或等于此版本
     * @param version 用于比较的版本
     * @return 是否不低于此版本
     */
    public boolean afterOrEquals(MinecraftVersion version) {
        return this.version >= version.version;
    }

    /**
     * 判断当前的版本是否处于一个版本,即等于此版本
     * @param version 用于比较的版本
     * @return 是否处于一个版本
     */
    public boolean equals(MinecraftVersion version) {
        return this.version == version.version;
    }

    public static MinecraftVersion current() {
        return CURRENT;
    }

    private static int getCurrentVersion() {
        //获取当前的游戏版本
        String versionStr = Bukkit.getBukkitVersion();
        if (versionStr.contains("-")) {
            versionStr = versionStr.substring(0, versionStr.indexOf("-"));
        }
        String[] split = versionStr.split("\\.");
        int minecraftVersion;
        minecraftVersion = 0;
        minecraftVersion += (Integer.parseInt(split[0]) * 10000);
        minecraftVersion += (Integer.parseInt(split[1]) * 100);
        if (split.length > 2)
            minecraftVersion += Integer.parseInt(split[2]);
        return minecraftVersion;
    }

}
