package czy.mooc.house.common.page;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
/*
 分页类
 */
@Getter
@Setter
public class Pagination {

    private int pageNum;//页码
    private int pageSize;//单页数据量
    private long totalCount;//总数
    private List<Integer> pages = Lists.newArrayList();

    public Pagination(Integer pageSize, Integer pageNum, Long totalCount) {
        this.totalCount = totalCount;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        for (int i = 1; i <= pageNum; i++) {
            pages.add(i);
        }
        //计算需要多少页来显示数据
        Long pageCount = totalCount / pageSize + ((totalCount % pageSize == 0) ? 0 : 1);
        if (pageCount > pageNum) {
            for (int i = pageNum + 1; i <= pageCount; i++) {
                pages.add(i);
            }
        }
    }
}
