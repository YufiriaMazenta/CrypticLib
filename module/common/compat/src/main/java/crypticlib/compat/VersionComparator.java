package crypticlib.compat;

public interface VersionComparator {

    /**
     * 比较两个版本谁更新
     * @param version1 第一个版本
     * @param version2 第二个版本
     * @return 如果第一个版本高于第二个版本,则返回1,版本相同则返回0,第一个版本小于第二个版本则返回-1
     */
    int compare(String version1, String version2);

}
