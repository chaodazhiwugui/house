package czy.mooc.house.web.controller;

import czy.mooc.house.biz.service.AgencyService;
import czy.mooc.house.biz.service.UserService;
import czy.mooc.house.common.constants.CommonConstants;
import czy.mooc.house.common.model.User;
import czy.mooc.house.common.result.ResultMsg;
import czy.mooc.house.common.utils.HashUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AgencyService agencyService;

    // ----------------------------注册流程------------------------------------

    /**
     * 注册提交:
     * 1、注册验证
     * 2、发送邮件
     * 3、验证失败重定向到注册页面
     *
     * @param account
     * @param modelMap
     * @return
     */
    @RequestMapping("accounts/register")
    public String accountsRegister(User account, ModelMap modelMap) {
        //根据account对象为依据判断是否注册页获取请求，若为null跳转到注册页
        if (account == null || account.getName() == null) {
            modelMap.put("agencyList", agencyService.getAllAgency());
            return "/user/accounts/register";
        }
        // 用户验证
        ResultMsg resultMsg = UserHelper.validate(account);
        if (resultMsg.isSuccess() && userService.addAccount(account)) {
            modelMap.put("email", account.getEmail());
            return "/user/accounts/registerSubmit";
        } else {
            return "redirect:/accounts/register?" + resultMsg.asUrlParams();
        }
    }

    /**
     * 激活校验
     *
     * @param key
     * @return
     */
    @RequestMapping("accounts/verify")
    public String verify(String key) {
        //判断key是否有效
        boolean result = userService.enable(key);
        if (result) {
            return "redirect:/index?" + ResultMsg.successMsg("激活成功").asUrlParams();
        } else {
            return "redirect:/accounts/register?" + ResultMsg.errorMsg("激活失败,请确认链接是否过期");
        }
    }

    // ----------------------------登录流程------------------------------------

    /**
     * 登录接口
     */
    @RequestMapping("/accounts/signin")
    public String signin(HttpServletRequest req) {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String target = req.getParameter("target");
        //账号密码为空则跳转到登录页面
        if (username == null || password == null) {
            req.setAttribute("target", target);
            return "/user/accounts/signin";
        }
        //验证用户名和密码
        User user = userService.auth(username, password);
        if (user == null) {
            return "redirect:/accounts/signin?" + "target=" + target + "&username=" + username + "&"
                    + ResultMsg.errorMsg("用户名或密码错误").asUrlParams();
        } else {
            //验证通过后存放user到session中
            HttpSession session = req.getSession(true);
            session.setAttribute(CommonConstants.USER_ATTRIBUTE, user);
            // session.setAttribute(CommonConstants.PLAIN_USER_ATTRIBUTE, user);
            return StringUtils.isNoneBlank(target) ? "redirect:" + target : "redirect:/index";
        }
    }

    /**
     * 登出操作
     *
     * @param request
     * @return
     */
    @RequestMapping("accounts/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.invalidate();
        return "redirect:/index";
    }

    // ---------------------个人信息页-------------------------

    /**
     * 1.提供个人信息页
     * 2.更新用户信息
     *
     * @param updateUser
     * @return
     */
    @RequestMapping("accounts/profile")
    public String profile(HttpServletRequest req, User updateUser) {
        //若没传email过来说明是查询个人信息（更新用户时会将隐藏的email一起发过来）
        if (updateUser.getEmail() == null) {
            return "/user/accounts/profile";
        }
        //更新用户
        userService.updateUser(updateUser, updateUser.getEmail());
        User query = new User();
        query.setEmail(updateUser.getEmail());
        //根据email查询到更新后的用户后放入session
        List<User> users = userService.getUserByQuery(query);
        req.getSession(true).setAttribute(CommonConstants.USER_ATTRIBUTE, users.get(0));
        return "redirect:/accounts/profile?" + ResultMsg.successMsg("更新成功").asUrlParams();
    }

    /**
     * 修改密码操作
     *
     * @param email
     * @param password
     * @param newPassword     新密码
     * @param confirmPassword 再次确认密码
     * @param mode
     * @return
     */
    @RequestMapping("accounts/changePassword")
    public String changePassword(String email, String password, String newPassword,
                                 String confirmPassword, ModelMap mode) {
        //验证用户是否存在
        User user = userService.auth(email, password);
        //若用户不存在或两次输入密码不一致，返回错误信息
        if (user == null || !confirmPassword.equals(newPassword)) {
            return "redirct:/accounts/profile?" + ResultMsg.errorMsg("密码错误").asUrlParams();
        }
        //修改密码
        User updateUser = new User();
        updateUser.setPasswd(HashUtils.encryPassword(newPassword));
        userService.updateUser(updateUser, email);
        return "redirect:/accounts/profile?" + ResultMsg.successMsg("更新成功").asUrlParams();
    }


    /**
     * 忘记密码
     *
     * @param username
     * @param modelMap
     * @return
     */
    @RequestMapping("accounts/remember")
    public String remember(String username, ModelMap modelMap) {
        if (StringUtils.isBlank(username)) {
            return "redirect:/accounts/signin?" + ResultMsg.errorMsg("邮箱不能为空").asUrlParams();
        }
        userService.resetNotify(username);
        modelMap.put("email", username);
        return "/user/accounts/remember";
    }

    @RequestMapping("accounts/reset")
    public String reset(String key, ModelMap modelMap) {
        String email = userService.getResetEmail(key);
        if (StringUtils.isBlank(email)) {
            return "redirect:/accounts/signin?" + ResultMsg.errorMsg("重置链接已过期").asUrlParams();
        }
        modelMap.put("email", email);
        modelMap.put("success_key", key);
        return "/user/accounts/reset";
    }

    @RequestMapping(value = "accounts/resetSubmit")
    public String resetSubmit(HttpServletRequest req, User user) {
        ResultMsg retMsg = UserHelper.validateResetPassword(user.getKey(), user.getPasswd(), user.getConfirmPasswd());
        if (!retMsg.isSuccess()) {
            String suffix = "";
            if (StringUtils.isNotBlank(user.getKey())) {
                suffix = "email=" + userService.getResetEmail(user.getKey()) + "&key=" + user.getKey() + "&";
            }
            return "redirect:/accounts/reset?" + suffix + retMsg.asUrlParams();
        }
        User updatedUser = userService.reset(user.getKey(), user.getPasswd());
        req.getSession(true).setAttribute(CommonConstants.USER_ATTRIBUTE, updatedUser);
        return "redirect:/index?" + retMsg.asUrlParams();
    }


}
