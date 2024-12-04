package crypticlib;

import org.bukkit.Bukkit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MinecraftVersion {

    private final int version;

    private MinecraftVersion(int version) {
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
    public boolean before(MinecraftVersion version) {
        return this.version < version.version;
    }

    /**
     * 判断当前的版本是否最高是一个版本，即低于或等于此版本
     * @param version 用于比较的版本
     * @return 是否最高是此版本
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
     * 判断当前的版本是否最少是一个版本，即高于或等于此版本
     * @param version 用于比较的版本
     * @return 是否最少是此版本
     */
    public boolean afterOrEquals(MinecraftVersion version) {
        return this.version >= version.version;
    }

    private static final Map<Integer, MinecraftVersion> versionMap = new ConcurrentHashMap<>();
    private static MinecraftVersion current;

    public static final MinecraftVersion V1_13 = new MinecraftVersion(11300);
    public static final MinecraftVersion V1_13_1 = new MinecraftVersion(11301);
    public static final MinecraftVersion V1_13_2 = new MinecraftVersion(11302);

    public static final MinecraftVersion V1_14 = new MinecraftVersion(11400);
    public static final MinecraftVersion V1_14_1 = new MinecraftVersion(11401);
    public static final MinecraftVersion V1_14_2 = new MinecraftVersion(11402);
    public static final MinecraftVersion V1_14_3 = new MinecraftVersion(11403);
    public static final MinecraftVersion V1_14_4 = new MinecraftVersion(11404);

    public static final MinecraftVersion V1_15 = new MinecraftVersion(11500);
    public static final MinecraftVersion V1_15_1 = new MinecraftVersion(11501);
    public static final MinecraftVersion V1_15_2 = new MinecraftVersion(11502);

    public static final MinecraftVersion V1_16 = new MinecraftVersion(11600);
    public static final MinecraftVersion V1_16_1 = new MinecraftVersion(11601);
    public static final MinecraftVersion V1_16_2 = new MinecraftVersion(11602);
    public static final MinecraftVersion V1_16_3 = new MinecraftVersion(11603);
    public static final MinecraftVersion V1_16_4 = new MinecraftVersion(11604);
    public static final MinecraftVersion V1_16_5 = new MinecraftVersion(11605);

    public static final MinecraftVersion V1_17 = new MinecraftVersion(11700);
    public static final MinecraftVersion V1_17_1 = new MinecraftVersion(11701);

    public static final MinecraftVersion V1_18 = new MinecraftVersion(11800);
    public static final MinecraftVersion V1_18_1 = new MinecraftVersion(11801);
    public static final MinecraftVersion V1_18_2 = new MinecraftVersion(11802);

    public static final MinecraftVersion V1_19 = new MinecraftVersion(11900);
    public static final MinecraftVersion V1_19_1 = new MinecraftVersion(11901);
    public static final MinecraftVersion V1_19_2 = new MinecraftVersion(11902);
    public static final MinecraftVersion V1_19_3 = new MinecraftVersion(11903);
    public static final MinecraftVersion V1_19_4 = new MinecraftVersion(11904);

    public static final MinecraftVersion V1_20 = new MinecraftVersion(12000);
    public static final MinecraftVersion V1_20_1 = new MinecraftVersion(12001);
    public static final MinecraftVersion V1_20_2 = new MinecraftVersion(12002);
    public static final MinecraftVersion V1_20_3 = new MinecraftVersion(12003);
    public static final MinecraftVersion V1_20_4 = new MinecraftVersion(12004);
    public static final MinecraftVersion V1_20_5 = new MinecraftVersion(12005);
    public static final MinecraftVersion V1_20_6 = new MinecraftVersion(12006);

    public static final MinecraftVersion V1_21 = new MinecraftVersion(12100);
    public static final MinecraftVersion V1_21_1 = new MinecraftVersion(12101);
    public static final MinecraftVersion V1_21_2 = new MinecraftVersion(12102);
    public static final MinecraftVersion V1_21_3 = new MinecraftVersion(12103);
    public static final MinecraftVersion V1_21_4 = new MinecraftVersion(12104);

    public static MinecraftVersion current() {
        if (current == null) {
            loadCurrentVersion();
        }
        return current;
    }

    private static void loadCurrentVersion() {
        //获取游戏版本
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
        if (!versionMap.containsKey(minecraftVersion)) {
            current = new MinecraftVersion(minecraftVersion);
        } else {
            current = versionMap.get(minecraftVersion);
        }
    }

}
