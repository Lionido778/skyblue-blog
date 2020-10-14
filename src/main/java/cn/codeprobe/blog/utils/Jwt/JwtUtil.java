package cn.codeprobe.blog.utils.Jwt;

import cn.codeprobe.blog.constatnts.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.Map;

public class JwtUtil {

    //加密key
    private static String key = "b0186e3a2a5a0eb1b79bab9ececcc7fc";
    //默认有效期 2小时
    private static long ttl = Constants.TimeValueInMillions.HOUR_2;


    public static String getKey() {
        return key;
    }

    public static void setKey(String key) {
        JwtUtil.key = key;
    }

    public static long getTtl() {
        return ttl;
    }

    public static void setTtl(long ttl) {
        JwtUtil.ttl = ttl;
    }

    /**
     * @param claims 载荷内容
     * @param ttl    自定义有效时长
     * @return
     */
    public static String createToken(Map<String, Object> claims, long ttl) {
        JwtUtil.ttl = ttl;
        return createToken(claims);
    }


    /**
     * 生成token
     *
     * @param claims 载荷内容
     * @return token
     */
    public static String createToken(Map<String, Object> claims) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)  //签发时间
                .signWith(SignatureAlgorithm.HS256, key);  //加密方式
        if (claims != null) {
            builder.setClaims(claims);  //claims信息
        }
        if (ttl > 0) {
            builder.setExpiration(new Date(nowMillis + ttl));  //过期时间戳
        }
        return builder.compact();
    }

    /**
     * 获取token中的claims信息
     *
     * @param token
     * @return
     */
    public static Claims parseJWT(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }


    public static String createRefreshToken(String userId, long ttl) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder().setId(userId)
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS256, key);
        if (ttl > 0) {
            builder.setExpiration(new Date(nowMillis + ttl));
        }
        return builder.compact();
    }


    //生成加密key
    public static void main(String[] args) {
        System.out.println(DigestUtils.md5DigestAsHex("blog_system".getBytes()));

        String refreshToken = createRefreshToken("123123123", Constants.TimeValueInMillions.MONTH);
        Claims claims = parseJWT(refreshToken);
        System.out.println(claims.getId());
    }

}

