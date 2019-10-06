package czy.mooc.house.web.controller;

import czy.mooc.house.biz.service.CommentService;
import czy.mooc.house.common.model.User;
import czy.mooc.house.web.interceptor.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 添加房屋评论
     */
    @RequestMapping(value = "/comment/leaveHouseComment", method = {RequestMethod.POST, RequestMethod.GET})
    public String leaveHouseComment(String content, Long houseId) {
        User user = UserContext.getUser();
        Long userId = user.getId();
        commentService.addHouseComment(houseId, content, userId);
        return "redirect:/house/detail?id=" + houseId;
    }

    /**
     * 添加博客评论
     */
    @RequestMapping(value = "/comment/leaveBlogComment", method = {RequestMethod.POST, RequestMethod.GET})
    public String leaveBlogComment(String content, Integer blogId) {
        User user = UserContext.getUser();
        Long userId = user.getId();
        commentService.addBlogComment(blogId, content, userId);
        return "redirect:/blog/detail?id=" + blogId;
    }

    /**
     * 删除房屋评论
     */
    @RequestMapping(value = "/comment/deleteHouseComment", method = {RequestMethod.POST, RequestMethod.GET})
    public String deleteHouseComment(Long userId, Long houseId) {

        commentService.deleteHouseComment(userId, houseId);
        return "redirect:/house/detail?id=" + houseId;
    }

    /**
     * 删除博客评论
     */
    @RequestMapping(value = "/comment/deleteBlogComment", method = {RequestMethod.POST, RequestMethod.GET})
    public String deleteBlogComment(Long userId, Integer blogId) {

        commentService.deleteBlogComment(userId, blogId);
        return "redirect:/blog/detail?id=" + blogId;
    }


}
