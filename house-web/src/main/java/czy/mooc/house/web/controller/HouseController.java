package czy.mooc.house.web.controller;

import czy.mooc.house.biz.service.*;
import czy.mooc.house.common.constants.CommonConstants;
import czy.mooc.house.common.constants.HouseUserType;
import czy.mooc.house.common.model.*;
import czy.mooc.house.common.page.PageData;
import czy.mooc.house.common.page.PageParams;
import czy.mooc.house.common.result.ResultMsg;
import czy.mooc.house.web.interceptor.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private CityService cityService;

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private CommentService commentService;

    /**
     * 获取房产列表
     * 1.实现分页
     * 2.支持小区搜索、类型搜索
     * 3.支持排序
     * 4.支持展示图片、价格、标题、地址等信息
     * pageSize——单页大小
     * pageNum——当前页数
     * query——若query.getName()不为空，则根据小区名查找房源
     */
    @RequestMapping("/house/list")
    public String houseList(Integer pageSize, Integer pageNum, House query, ModelMap modelMap) {
        //查询房产列表
        PageData<House> ps = houseService.queryHouse(query, PageParams.build(pageSize, pageNum));
        //获取热点房产
        List<House> hotHouses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", hotHouses);
        modelMap.put("ps", ps);
        modelMap.put("vo", query);
        return "/house/listing";
    }

    /**
     * 返回添加房产页面
     */
    @RequestMapping("/house/toAdd")
    public String toAdd(ModelMap modelMap) {
        modelMap.put("citys", cityService.getAllCitys());
        modelMap.put("communitys", houseService.getAllCommunitys());
        return "/house/add";
    }

    /**
     * 添加房产
     */
    @RequestMapping("/house/add")
    public String doAdd(House house) {
        User user = UserContext.getUser();
        house.setState(CommonConstants.HOUSE_STATE_UP);//设置上架状态
        //添加房产
        houseService.addHouse(house, user);
        return "redirect:/house/ownlist";
    }

    /**
     * 个人房产信息
     */
    @RequestMapping("/house/ownlist")
    public String ownlist(House house, Integer pageNum, Integer pageSize, ModelMap modelMap) {
        User user = UserContext.getUser();
        house.setUserId(user.getId());
        house.setBookmarked(false);
        modelMap.put("ps", houseService.queryHouse(house, PageParams.build(pageSize, pageNum)));
        modelMap.put("pageType", "own");
        return "/house/ownlist";
    }

    /**
     * 查询房屋详情，并且查询绑定的经纪机构
     */
    @RequestMapping("/house/detail")
    public String houseDetail(Long id, ModelMap modelMap) {
        //查询房源
        House house = houseService.queryOneHouse(id);
        //查询和房源查询绑定的用户
        HouseUser houseUser = houseService.getHouseUser(id);
        //查看房产详情时房产热度+1
        recommendService.increase(id);
        //查询用户评论
        List<Comment> comments = commentService.getHouseComments(id, 8);
        //查询绑定房源的经纪机构
        if (houseUser.getUserId() != null && !houseUser.getUserId().equals(0L)) {
            modelMap.put("agent", agencyService.getAgentDeail(houseUser.getUserId()));
        }
        //查询热点房源
        List<House> rcHouses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", rcHouses);
        modelMap.put("house", house);
        modelMap.put("commentList", comments);
        return "/house/detail";
    }

    /**
     * 留言
     */
    @RequestMapping("/house/leaveMsg")
    public String houseMsg(UserMsg userMsg) {
        //添加留言
        houseService.addUserMsg(userMsg);
        return "redirect:/house/detail?id=" + userMsg.getHouseId() + ResultMsg.successMsg("留言成功").asUrlParams();
    }

    /**
     * 评分
     */
    @ResponseBody
    @RequestMapping("/house/rating")
    public ResultMsg houseRate(Double rating, Long id) {
        //更新评分
        houseService.updateRating(id, rating);
        return ResultMsg.successMsg("ok");
    }


    /**
     * 收藏
     */
    @ResponseBody
    @RequestMapping("/house/bookmark")
    public ResultMsg bookmark(Long id) {
        User user = UserContext.getUser();
        //绑定用户和房产的关系
        houseService.bindUser2House(id, user.getId(), true);//true表示收藏、false表示售卖
        return ResultMsg.successMsg("ok");
    }

    /**
     * 取消收藏
     */
    @ResponseBody
    @RequestMapping("/house/unbookmark")
    public ResultMsg unbookmark(Long id) {
        User user = UserContext.getUser();
        //取消用户——房屋的收藏关系
        houseService.unbindUser2House(id, user.getId(), HouseUserType.BOOKMARK);
        return ResultMsg.successMsg("ok");
    }

    /**
     * 下架房屋
     */
    @RequestMapping(value = "/house/del")
    public String delsale(Long id, String pageType) {
        User user = UserContext.getUser();
        //取消用户和房屋的售卖关系
        houseService.unbindUser2House(id, user.getId(), pageType.equals("own") ? HouseUserType.SALE : HouseUserType.BOOKMARK);
        return "redirect:/house/ownlist";
    }

    /**
     * 返回收藏列表页
     */
    @RequestMapping("/house/bookmarked")
    public String bookmarked(House house, Integer pageNum, Integer pageSize, ModelMap modelMap) {
        User user = UserContext.getUser();
        house.setBookmarked(true);
        house.setUserId(user.getId());
        modelMap.put("ps", houseService.queryHouse(house, PageParams.build(pageSize, pageNum)));
        modelMap.put("pageType", "book");
        return "/house/ownlist";
    }
}
