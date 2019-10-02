package czy.mooc.house.biz.mapper;

import czy.mooc.house.common.model.*;
import czy.mooc.house.common.page.PageParams;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HouseMapper {

    //根据House对象和分页信息查询符合条件的房源
    List<House> selectPageHouses(@Param("house") House house, @Param("pageParams") PageParams pageParams);

    Long selectPageCount(@Param("house") House query);

    int insert(User account);

    List<Community> selectCommunity(Community community);

    int insert(House house);

    HouseUser selectHouseUser(@Param("userId") Long userId, @Param("id") Long houseId, @Param("type") Integer integer);

    HouseUser selectSaleHouseUser(@Param("id") Long houseId);

    int insertHouseUser(HouseUser houseUser);

    int insertUserMsg(UserMsg userMsg);

    int updateHouse(House updateHouse);

    int downHouse(Long id);

    int deleteHouseUser(@Param("id") Long id, @Param("userId") Long userId, @Param("type") Integer value);

}
