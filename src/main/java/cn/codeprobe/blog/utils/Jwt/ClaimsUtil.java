package cn.codeprobe.blog.utils.Jwt;

import cn.codeprobe.blog.pojo.User;
import io.jsonwebtoken.Claims;

import java.util.HashMap;
import java.util.Map;

public class ClaimsUtil {

    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String STATE = "state";
    private static final String ROLES = "roles";
    private static final String AVATAR = "avatar";
    private static final String EMAIL = "email";
    private static final String SIGN = "sign";
    private static final String TERMINAL = "terminal";

    /**
     * 将登录用户和登录终端解析进claims
     *
     * @param user     登陆用户
     * @param terminal 登陆终端
     * @return
     */
    public static Map<String, Object> User2Claims(User user, String terminal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(ID, user.getId());
        claims.put(USERNAME, user.getUsername());
        claims.put(STATE, user.getState());
        claims.put(ROLES, user.getRoles());
        claims.put(AVATAR, user.getAvatar());
        claims.put(EMAIL, user.getEmail());
        claims.put(SIGN, user.getSign());
        claims.put(TERMINAL, terminal);
        return claims;
    }

    /**
     * 从claims解析出登录用户
     *
     * @param claims
     * @return
     */
    public static User Claims2User(Claims claims) {
        User user = new User();
        user.setId((String) claims.get(ID));
        user.setUsername((String) claims.get(USERNAME));
        user.setState((String) claims.get(STATE));
        user.setRoles((String) claims.get(ROLES));
        user.setAvatar((String) claims.get(AVATAR));
        user.setEmail((String) claims.get(EMAIL));
        user.setSign((String) claims.get(SIGN));
        return user;
    }

    /**
     * 从Claims解析出登录终端
     *
     * @param claims
     * @return
     */
    public static String Claims2Terminal(Claims claims) {
        return (String) claims.get(TERMINAL);
    }

}
