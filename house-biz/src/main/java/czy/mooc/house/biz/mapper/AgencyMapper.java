package czy.mooc.house.biz.mapper;

import java.util.List;

import czy.mooc.house.common.page.PageParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import czy.mooc.house.common.model.Agency;
import czy.mooc.house.common.model.User;

@Mapper
public interface AgencyMapper {
  
    List<Agency> select(Agency agency);

    int insert(Agency agency);

    List<User>	selectAgent(@Param("user") User user,@Param("pageParams") PageParams pageParams);

	Long selectAgentCount(@Param("user")User user);

}
