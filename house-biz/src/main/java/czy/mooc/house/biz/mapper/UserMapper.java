package czy.mooc.house.biz.mapper;

import czy.mooc.house.common.model.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    List<User> selectUsers();

    int insert(User account);

    int delete(String email);

    int update(User updateUser);

    List<User> selectUsersByQuery(User user);
}
