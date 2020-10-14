package cn.codeprobe.blog.utils.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidateUtil {

    public static final String regexEmail = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

    public static final String regexPhone = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";

    /**
     * 判断邮箱格式是否正确
     *
     * @param emailAddr 邮箱地址
     * @return
     */
    public static Boolean isEmailFormatOK(String emailAddr) {
        Pattern p = Pattern.compile(regexEmail);
        Matcher m = p.matcher(emailAddr);
        return m.matches();
    }

    /**
     * 判断手机格式是否正确
     *
     * @param phone 手机号
     * @return
     */
    public static boolean isPhone(String phone) {
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regexPhone);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }
}
