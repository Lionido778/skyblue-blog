package cn.codeprobe.blog.intercept;

import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.constatnts.Constants;
import cn.codeprobe.blog.utils.Jwt.CookieUtil;
import cn.codeprobe.blog.utils.common.RedisUtil;
import cn.codeprobe.blog.utils.common.StringUtil;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Component
@Slf4j
public class Interceptor extends HandlerInterceptorAdapter {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Gson gson;

    /**
     * 通过注解的方式拦截所有提交的方法，防止短时间内重复提交。时间周期为30秒
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Annotation methodAnnotation = handlerMethod.getMethodAnnotation(CheckRepeatCommit.class);
            //判断是否有 提交事件
            if (methodAnnotation != null) {
                String tokenKey = CookieUtil.getCookieValue(request, Constants.Cookie.COOKIE_TOKEN_KEY);
                //tokenKey作为id,提交事件必须是登陆状态。
                if (!StringUtil.isEmpty(tokenKey)) {
                    String commitStatus = (String) redisUtil.get(Constants.User.KEY_REPEAT_COMMIT + tokenKey);
                    //通过redis的缓存记录判断是否短时间内重复提交
                    if (!StringUtil.isEmpty(commitStatus)) {
                        response.setCharacterEncoding("UTF-8");
                        response.setContentType("application/json");
                        ResponseResult repeatCommit = ResponseResult.FAILED("提交太过频繁，请稍后重试");
                        PrintWriter writer = response.getWriter();
                        writer.write(gson.toJson(repeatCommit));
                        writer.flush();
                        return false;
                    }
                    redisUtil.set(Constants.User.KEY_REPEAT_COMMIT + tokenKey, "has commit", Constants.TimeValueInSeconds.MIN / 2);
                }
            }
            Method method = handlerMethod.getMethod();
            String methodName = method.getName();
            log.info("has executed method name ==> " + methodName);
        }
        //true 放行，false，拦截
        return true;
    }
}
