package crypticlib.ui.menu;

/**
 * 表示一个UI是多页的接口
 */
public interface Multipage {

    void nextPage();

    void previousPage();

    Integer page();

    void page(int page);

    Integer maxPage();

}
