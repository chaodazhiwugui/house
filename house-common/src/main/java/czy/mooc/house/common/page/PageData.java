package czy.mooc.house.common.page;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/*
 返回给前端的分页数据
 */
@Getter
@Setter
public class PageData<T> {

    private List<T> list;//存放需要展示的对象
    private Pagination pagination;//封装页数、单页数据量、数据总量等

    public PageData(Pagination pagination, List<T> list) {
        this.pagination = pagination;
        this.list = list;
    }

    /**
     * 构建分页结果
     * @param list 存放返回给前端的数据
     * @param count  数据总量
     * @param pageSize  单页数据量
     * @param pageNum  页数
     * @param <T>
     * @return
     */
    public static <T> PageData<T> buildPage(List<T> list, Long count, Integer pageSize, Integer pageNum) {
        Pagination pagination = new Pagination(pageSize, pageNum, count);
        return new PageData<>(pagination, list);
    }

}
