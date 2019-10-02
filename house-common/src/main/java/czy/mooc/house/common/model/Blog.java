package czy.mooc.house.common.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/*
 博客类
 */
@Getter
@Setter
public class Blog {
  private Integer id;
  private String  tags;
  private String  content;
  private String  title;
  private Date    createTime;
  private String  digest;
  
  private List<String> tagList = Lists.newArrayList();

}
