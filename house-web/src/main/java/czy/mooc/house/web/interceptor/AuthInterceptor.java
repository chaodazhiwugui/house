package czy.mooc.house.web.interceptor;

import com.google.common.base.Joiner;
import czy.mooc.house.common.constants.CommonConstants;
import czy.mooc.house.common.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * AuthInterceptor拦截器
 * 若用户已经登录，则存放user对象到ThreadLocal中，然后在方法结束后移除
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Map<String, String[]> map = request.getParameterMap();
        map.forEach((k, v) -> {
            if (k.equals("errorMsg") || k.equals("successMsg") || k.equals("target")) {
                request.setAttribute(k, Joiner.on(",").join(v));
            }
        });
        //判断是否访问静态资源或者错误页面
        String reqUri = request.getRequestURI();
        if (reqUri.startsWith("/static") || reqUri.startsWith("/error")) {
            return true;
        }
        HttpSession session = request.getSession(true);//传入true表示如果没有session就创建一个，默认为false
        User user = (User) session.getAttribute(CommonConstants.USER_ATTRIBUTE);
        if (user != null) {
            UserContext.setUser(user);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        UserContext.remove();
    }


}
