package crypticlib.perm;

public interface PermManager {

    PermManager regPerm(PermInfo permission);

    boolean hasPermission(Object permissible, String permission);

}
