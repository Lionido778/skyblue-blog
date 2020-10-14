package cn.codeprobe.blog.service;

import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.response.ResponseResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {

    ResponseResult initManagerAccount(User user, HttpServletRequest request);

    void createCaptcha(HttpServletRequest request, HttpServletResponse response, String captchaKey) throws Exception;

    ResponseResult sendVerifyCode(HttpServletRequest request, String type, String emailAddress);

    ResponseResult sendSms(String phoneNumbers);

    ResponseResult register(User user, String type, String emailVerifyCode, String captchaCode, String captchaKey, HttpServletRequest request);

    ResponseResult doLogin(String captcha, String captchaKey, User user, String terminal, HttpServletRequest request, HttpServletResponse response);

    // 检查用户登陆状态
    User checkLoginStatus();

    User parseByTokenKey2User(String tokenKey);

    String parseByTokenKey2Terminal(String tokenKey);

    ResponseResult getUserInfo(String userId);

    ResponseResult checkUsername(String username);

    ResponseResult checkEmail(String email);

    ResponseResult updateUserInfo(String userId, User user, HttpServletRequest request);

    ResponseResult deleteUser(String userId);

    ResponseResult listUsers(int page, int size);

    ResponseResult updatePassword(User user, String verifyCode);

    ResponseResult updateEmail(String email, String verifyCode);

    ResponseResult doLogout(HttpServletRequest request, HttpServletResponse response);

    ResponseResult getQrCodeLogin() ;

    ResponseResult checkQrCodeLoginState(String loginId);

    ResponseResult updateQrCodeLoginState(String loginId);
}
