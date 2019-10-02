package czy.mooc.house.common.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/*
 留言类
 */
@Getter
@Setter
public class UserMsg {

	private Long id;
	private String msg;
	private Long  userId;
	private Date  createTime;
	private Long  agentId;
	private Long  houseId;
	private String email;
	private String userName;
}
