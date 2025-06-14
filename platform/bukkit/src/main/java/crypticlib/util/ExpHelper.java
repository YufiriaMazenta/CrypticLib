package crypticlib.util;

import org.bukkit.entity.Player;

public class ExpHelper {

    /**
     * 获取玩家当前的所有经验值
     * @param player
     * @return
     */
    public static int getPlayerExp(Player player) {
        int level = player.getLevel();
        float expProgress = player.getExp();
        return (int) (getLevelTotalExp(level) + getLevelUpgradeExp(level) * expProgress);
    }

    /**
     * 获取某个等级升级所需的经验数量
     * @param nowLevel 当前的等级
     * @return 升级所需的经验数量
     */
    public static int getLevelUpgradeExp(int nowLevel) {
        if (nowLevel <= 15) {
            return 2 * nowLevel + 7;
        } else if (nowLevel <= 30) {
            return 5 * nowLevel - 38;
        } else {
            return 9 * nowLevel - 158;
        }
    }

    /**
     * 获取升级到某个等级需要的经验值
     * @param level
     * @return
     */
    public static int getLevelTotalExp(int level) {
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    /**
     * 获取某经验值对应的等级，包含当前的升级进度
     * @param exp
     * @return
     */
    public static double getLevelFromExp(long exp) {
        int level = getIntLevelFromExp(exp);

        float remainder = exp - (float) getLevelTotalExp(level);

        float progress = remainder / getLevelUpgradeExp(level);

        return ((double) level) + progress;
    }

    /**
     * 获取经验所对应的等级，不包含当前的升级进度
     *
     * @param exp
     * @return
     */
    public static int getIntLevelFromExp(long exp) {
        if (exp > 1395) {
            return (int) ((Math.sqrt(72 * exp - 54215D) + 325) / 18);
        }
        if (exp > 315) {
            return (int) (Math.sqrt(40 * exp - 7839D) / 10 + 8.1);
        }
        if (exp > 0) {
            return (int) (Math.sqrt(exp + 9D) - 3);
        }
        return 0;
    }

    /**
     * 更改玩家当前的经验值，会重新计算玩家的等级
     * @param player
     * @param exp
     */
    public static void changeExp(Player player, int exp) {
        exp += getPlayerExp(player);

        if (exp < 0) {
            exp = 0;
        }

        double levelAndExp = getLevelFromExp(exp);
        int level = (int) levelAndExp;
        player.setLevel(level);
        player.setExp((float) (levelAndExp - level));
    }

}
