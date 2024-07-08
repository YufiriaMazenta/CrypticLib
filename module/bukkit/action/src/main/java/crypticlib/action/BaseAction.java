package crypticlib.action;

/**
 * 基础的动作抽象类，实现了next和setNext方法
 */
public abstract class BaseAction implements Action {

    protected Action next;

    @Override
    public Action next() {
        return next;
    }

    @Override
    public Action setNext(Action next) {
        this.next = next;
        return this;
    }

}
