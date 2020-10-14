package cn.codeprobe.blog.utils.Jwt;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class CookieUtil {

    // TODO: 域名要使用工具类动态获取
    private static final String domain = "localhost";
    // 默认有效期
    private static final int default_ttl = 60 * 60 * 24 * 180;  //180天

    /**
     * 创建 cookie
     *
     * @param value
     * @param cookieKey
     */
    public static void setupCookie(HttpServletResponse response, String cookieKey, String value) {
        setupCookie(response, cookieKey, value, default_ttl);
    }

    public static void setupCookie(HttpServletResponse response, String cookieKey, String value, int ttl) {
        Cookie cookie = new Cookie(cookieKey, value);
        cookie.setMaxAge(ttl);
        //实际生产环境需要我们，设置域名。这里不设置默认为IP地址
        //cookie.setDomain(domain);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 删除 cookie
     *
     * @param value
     * @param cookieKey
     */
    public static void delCookie(HttpServletResponse response, String cookieKey, String value) {
        setupCookie(response, cookieKey, null, 0);
    }

    /**
     * 获取Cookie值 == tokenKey
     *
     * @param request
     * @param cookieKey
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieKey) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.info("checkLoginStatus cookies is null");
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieKey.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }


}
