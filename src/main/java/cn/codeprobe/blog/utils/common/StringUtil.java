package cn.codeprobe.blog.utils.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class StringUtil {

    private static BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();


    /**
     * 判断文本是否为空
     *
     * @param text 字符串文本
     * @return
     */
    public static Boolean isEmpty(String text) {
        return text == null || text.length() == 0;
    }


    /**
     * 密码加密
     *
     * @param rawPassword 原始密码
     * @return
     */
    public static String enPassword(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    /**
     * 密码匹配
     *
     * @param rawPassword 原始密码
     * @param enPassword  加密密码
     * @return
     */
    public static boolean matchPassword(String rawPassword, String enPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, enPassword);
    }
}
