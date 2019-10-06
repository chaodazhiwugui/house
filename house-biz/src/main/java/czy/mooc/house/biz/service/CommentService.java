package czy.mooc.house.biz.service;

import czy.mooc.house.biz.mapper.CommentMapper;
import czy.mooc.house.common.constants.CommonConstants;
import czy.mooc.house.common.model.Comment;
import czy.mooc.house.common.model.User;
import czy.mooc.house.common.utils.BeanHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserService userService;


    /**
     * 添加评论
     */
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Long houseId, Integer blogId, String content, Long userId, int type) {
        Comment comment = new Comment();
        if (type == CommonConstants.COMMENT_HOUSE_TYPE) {//房屋评论
            comment.setHouseId(houseId);
        } else {//博客评论
            comment.setBlogId(blogId);
        }
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setType(type);
        BeanHelper.onInsert(comment);
        BeanHelper.setDefaultProp(comment, Comment.class);
        commentMapper.insert(comment);
    }

    /**
     * 删除评论
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long houseId, Integer blogId, int type) {
        Comment comment = new Comment();
        if (type == CommonConstants.COMMENT_HOUSE_TYPE) {//房屋评论
            comment.setHouseId(houseId);
        } else {//博客评论
            comment.setBlogId(blogId);
        }
        comment.setUserId(userId);
        comment.setType(type);
        BeanHelper.setDefaultProp(comment, Comment.class);
        commentMapper.delete(comment);
    }

    /**
     * 获取房屋评论
     */
    public List<Comment> getHouseComments(long houseId, int size) {
        List<Comment> comments = commentMapper.selectComments(houseId, size);
        //对评论用户设置用户名和头像路径
        comments.forEach(comment -> {
            User user = userService.getUserById(comment.getUserId());
            comment.setAvatar(user.getAvatar());
            comment.setUserName(user.getName());
        });
        return comments;
    }

    /**
     * 获取博客评论
     */
    public List<Comment> getBlogComments(long blogId, int size) {
        List<Comment> comments = commentMapper.selectBlogComments(blogId, size);
        comments.forEach(comment -> {
            User user = userService.getUserById(comment.getUserId());
            comment.setUserName(user.getName());
            comment.setAvatar(user.getAvatar());
        });
        return comments;
    }


    /**
     * 添加房屋评论
     */
    public void addHouseComment(Long houseId, String content, Long userId) {
        addComment(houseId, null, content, userId, 1);
    }

    /**
     * 添加博客评论
     */
    public void addBlogComment(int blogId, String content, Long userId) {
        addComment(null, blogId, content, userId, CommonConstants.COMMENT_BLOG_TYPE);
    }


    /**
     * 删除房屋评论
     */
    public void deleteHouseComment(Long userId, Long houseId) {
        deleteComment(userId, houseId, 0, CommonConstants.COMMENT_HOUSE_TYPE);
    }


    /**
     * 删除博客评论
     */
    public void deleteBlogComment(Long userId, Integer blogId) {
        deleteComment(userId, 0L, blogId, CommonConstants.COMMENT_BLOG_TYPE);
    }

}
