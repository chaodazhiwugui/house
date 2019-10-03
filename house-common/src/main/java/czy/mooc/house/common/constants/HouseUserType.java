package czy.mooc.house.common.constants;

public enum HouseUserType {

	SALE(1),//售卖
	BOOKMARK(2);//收藏
	
	public final Integer value;
	
	private HouseUserType(Integer value){
		this.value = value;
	}
}
