package crypticlib.perm;

public interface PermManager<P> {

    PermManager<P> regPerm(PermInfo permission);

    boolean hasPermission(P permissible, String permission);

}
