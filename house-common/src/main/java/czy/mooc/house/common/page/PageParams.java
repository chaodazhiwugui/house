package czy.mooc.house.common.page;

import lombok.Getter;
import lombok.Setter;

/*
 分页实体
 */
@Getter
@Setter
public class PageParams {

    //默认单页显示5条数据
    private static final Integer PAGE_SIZE = 5;

    private Integer pageSize;//单页显示数量
    private Integer pageNum; //页数
    private Integer offset;  //当前页第一位
    private Integer limit;   //当前页最后一位

    public static PageParams build(Integer pageSize, Integer pageNum) {
        if (pageSize == null) {
            pageSize = PAGE_SIZE;
        }
        if (pageNum == null) {
            pageNum = 1;
        }
        return new PageParams(pageSize, pageNum);
    }

    public PageParams() {
        this(PAGE_SIZE, 1);
    }

    public PageParams(Integer pageSize, Integer pageNum) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.offset = pageSize * (pageNum - 1);
        this.limit = pageSize;
    }

}
