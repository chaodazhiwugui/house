package czy.mooc.house.biz.service;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import czy.mooc.house.biz.mapper.HouseMapper;
import czy.mooc.house.common.constants.HouseUserType;
import czy.mooc.house.common.model.*;
import czy.mooc.house.common.page.PageData;
import czy.mooc.house.common.page.PageParams;
import czy.mooc.house.common.utils.BeanHelper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HouseService {

    @Autowired
    private HouseMapper houseMapper;

    @Value("${file.prefix}")
    private String imgPrefix;

    @Autowired
    private FileService fileService;

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private MailService mailService;


    /**
     * 1.查询小区
     * 2.添加图片服务器地址前缀
     * 3.构建分页结果
     */
    public PageData<House> queryHouse(House query, PageParams pageParams) {
        List<House> houses;
        //若小区名不为空，则根据小区名查找房源
        if (!Strings.isNullOrEmpty(query.getName())) {
            Community community = new Community();
            community.setName(query.getName());
            List<Community> communities = houseMapper.selectCommunity(community);
            if (!communities.isEmpty()) {
                query.setCommunityId(communities.get(0).getId());
            }
        }
        //添加图片服务器地址前缀
        houses = queryAndSetImg(query, pageParams);
        //查询到的数量
        Long count = houseMapper.selectPageCount(query);
        return PageData.buildPage(houses, count, pageParams.getPageSize(), pageParams.getPageNum());
    }

    /**
     * 查询符合条件的房源，并且设置图片地址
     */
    public List<House> queryAndSetImg(House query, PageParams pageParams) {
        //根据House对象和分页信息查询符合条件的房源，然后设置图片地址
        List<House> houses = houseMapper.selectPageHouses(query, pageParams);
        houses.forEach(h -> {
            h.setFirstImg(imgPrefix + h.getFirstImg());
            h.setImageList(h.getImageList().stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
            h.setFloorPlanList(h.getFloorPlanList().stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
        });
        return houses;
    }

    public List<Community> getAllCommunitys() {
        Community community = new Community();
        return houseMapper.selectCommunity(community);
    }

    /**
     * 添加房屋图片
     * 添加户型图片
     * 插入房产信息
     * 绑定用户——房产的关系
     */
    public void addHouse(House house, User user) {
        //若用户上传房产图片，则转换成string类型后保存到本地
        if (CollectionUtils.isNotEmpty(house.getHouseFiles())) {
            String images = Joiner.on(",").join(fileService.getImgPaths(house.getHouseFiles()));
            house.setImages(images);
        }
        if (CollectionUtils.isNotEmpty(house.getFloorPlanFiles())) {
            String images = Joiner.on(",").join(fileService.getImgPaths(house.getFloorPlanFiles()));
            house.setFloorPlan(images);
        }
        BeanHelper.onInsert(house);//设置创建时间
        //添加房产
        houseMapper.insert(house);
        //绑定用户和房产的关系
        bindUser2House(house.getId(), user.getId(), false);//false表示不是收藏，而是售卖
    }

    /*
     * 绑定用户和房产
     */
    public void bindUser2House(Long houseId, Long userId, boolean collect) {
        //查看用户和房产是否已经绑定
        HouseUser existhouseUser = houseMapper.selectHouseUser(userId, houseId, collect ? HouseUserType.BOOKMARK.value : HouseUserType.SALE.value);
        if (existhouseUser != null) {
            return;
        }
        HouseUser houseUser = new HouseUser();
        houseUser.setHouseId(houseId);
        houseUser.setUserId(userId);
        houseUser.setType(collect ? HouseUserType.BOOKMARK.value : HouseUserType.SALE.value);

        BeanHelper.setDefaultProp(houseUser, HouseUser.class);
        BeanHelper.onInsert(houseUser);
        //插入user-house关系
        houseMapper.insertHouseUser(houseUser);
    }

    public HouseUser getHouseUser(Long houseId) {
        HouseUser houseUser = houseMapper.selectSaleHouseUser(houseId);
        return houseUser;
    }

    /**
     * 根据id查询房源
     */
    public House queryOneHouse(Long id) {
        House query = new House();
        query.setId(id);
        List<House> houses = queryAndSetImg(query, PageParams.build(1, 1));
        if (!houses.isEmpty()) {
            return houses.get(0);
        }
        return null;
    }

    /**
     * 给经纪人留言
     */
    public void addUserMsg(UserMsg userMsg) {
        BeanHelper.onInsert(userMsg);
        houseMapper.insertUserMsg(userMsg);
        User agent = agencyService.getAgentDeail(userMsg.getAgentId());
        //发送邮件
        mailService.sendMail("来自用户" + userMsg.getEmail() + "的留言", userMsg.getMsg(), agent.getEmail());
    }

    /**
     * 更新评分
     */
    public void updateRating(Long id, Double rating) {
        House house = queryOneHouse(id);
        //取出旧的分数值
        Double oldRating = house.getRating();
        //如果是第一次评分则直接设置，否则取分数平均值，最高不超过5
        Double newRating = oldRating.equals(0D) ? rating : Math.min((oldRating + rating) / 2, 5);
        House updateHouse = new House();
        updateHouse.setId(id);
        updateHouse.setRating(newRating);
        BeanHelper.onUpdate(updateHouse);
        houseMapper.updateHouse(updateHouse);
    }

    /**
     * 解绑用户和房产
     */
    public void unbindUser2House(Long id, Long userId, HouseUserType type) {
        if (type.equals(HouseUserType.SALE)) {
            houseMapper.downHouse(id);//下架房产
        } else {
            houseMapper.deleteHouseUser(id, userId, type.value);//删除绑定关系
        }

    }

}
