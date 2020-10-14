package cn.codeprobe.blog.controller.user;

import cn.codeprobe.blog.intercept.CheckRepeatCommit;
import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.UserService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserApi {

    @Autowired
    private UserService userService;

    /**
     * 初始化管理员账户
     *
     * @param user
     * @return
     */
    @CheckRepeatCommit
    @PostMapping("/admin_account")
    public ResponseResult initManagerAccount(@RequestBody User user, HttpServletRequest request) {
        log.info("username ==> " + user.getUsername());
        log.info("password ==> " + user.getPassword());
        log.info("email ==> " + user.getEmail());
        return userService.initManagerAccount(user, request);
    }

    /**
     * 注册
     *
     * @param user
     * @return
     */
    @CheckRepeatCommit
    @PostMapping("/join_in")
    public ResponseResult register(@RequestBody User user,
                                   @RequestParam("type") String type,
                                   @RequestParam("verify_code") String verifyCode,
                                   @RequestParam("captchaCode") String captchaCode,
                                   @RequestParam("captchaKey") String captchaKey,
                                   HttpServletRequest request) {
        return userService.register(user, type, verifyCode, captchaCode, captchaKey, request);
    }

    /**
     * 登录
     * <p>
     * 需要提交的数据
     * 1. 用户账号可以是昵称，可以是邮箱（唯一）
     * 2. 密码
     * 3. 图灵验证码
     * 4. 图灵验证码的 key
     * </P>
     *
     * @param captcha
     * @param captchaKey
     * @param user
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/login/{captcha}/{captchaKey}")
    public ResponseResult doLogin(@PathVariable("captcha") String captcha,
                                  @PathVariable("captchaKey") String captchaKey,
                                  @RequestBody User user,
                                  @RequestParam(value = "terminal", required = false) String terminal,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        return userService.doLogin(captcha, captchaKey, user, terminal, request, response);
    }

    /**
     * 获取图灵验证码
     *
     * @return
     */
    @GetMapping("/captcha/{captchaKey}")
    public void getCaptcha(HttpServletRequest request, HttpServletResponse response, @PathVariable("captchaKey") String captchaKey) throws Exception {
        userService.createCaptcha(request, response, captchaKey);
    }

    /**
     * 发送邮件验证码
     *
     * @param emailAddress
     * @return
     */
    @GetMapping("/verify_code/{type}/{email}")
    public ResponseResult sendEmail(HttpServletRequest request, @PathVariable("type") String type, @PathVariable("email") String emailAddress) {
        return userService.sendVerifyCode(request, type, emailAddress);
    }


    @GetMapping("/verify_code/{phone}")
    public ResponseResult sendSms(@PathVariable("phone") String phone) {
        return userService.sendSms(phone);
    }


    /**
     * 修改密码
     * <p>
     * 普通做法：通过旧密码比对来更新密码（必须登录）
     * <p>
     *
     * <p>
     * 另一种做法：既可以修改密码，也可以找回密码
     * 发送验证码到邮箱/手机---> 通过比较验证码是否正确来判断对应邮箱/手机号所注册的账号是否属于你
     * </p>
     * 步骤：
     * 1. 用户填写邮箱
     * 2. 用户获取验证码 type = forget
     * 3. 填写验证码
     * 4. 填写新密码
     * 5. 提交数据
     *
     * @return
     */
    @PutMapping("/password/{verifyCode}")
    public ResponseResult updatePassword(@RequestBody User user, @PathVariable("verifyCode") String verifyCode) {
        return userService.updatePassword(user, verifyCode);
    }

    /**
     * 获取用户信息
     *
     * @param userId
     * @return
     */
    @GetMapping("/user_info/{userId}")
    public ResponseResult getUserInfo(@PathVariable("userId") String userId) {
        return userService.getUserInfo(userId);
    }

    /**
     * 修改用户信息
     * <p>
     * 允许用户修改的内容：
     * 1. 头像
     * 2. 用户名 （唯一）
     * 3. 签名
     * 4. 密码   （单独一个接口）
     * 5. Email   (单独一个接口修改，唯一)
     * </p>
     *
     * @param user
     * @return
     */
    @PutMapping("/user_info/{userId}")
    public ResponseResult updateUserInfo(@PathVariable("userId") String userId, @RequestBody User user,
                                         HttpServletRequest request) {
        return userService.updateUserInfo(userId, user, request);
    }

    /**
     * 获取用户列表
     * <p>
     * 1. 检查登陆状态，判断当前用户是谁
     * 2. 该用户是否具有管理员权限
     * </p>
     *
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("@permission.admin()")  //通过注解的方式来控制权限
    @GetMapping("/list")
    public ResponseResult listUser(@RequestParam("page") int page, @RequestParam("size") int size) {
        return userService.listUsers(page, size);
    }

    /**
     * 删除用户
     *
     * @param userId
     * @return
     */
    @DeleteMapping("/{userId}")
    public ResponseResult deleteUser(@PathVariable("userId") String userId,
                                     HttpServletResponse response, HttpServletRequest request) {
        return userService.deleteUser(userId);
    }


    /**
     * 检查用户名是否被注册
     *
     * @param username
     * @return
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "表示用户名未被占用"),
            @ApiResponse(code = 40000, message = "表示用户名已被占用")
    })
    @CheckRepeatCommit
    @PostMapping("/user_name")
    public ResponseResult checkUsername(String username) {
        return userService.checkUsername(username);
    }

    /**
     * 检查邮箱是否被注册
     *
     * @param email
     * @return
     */
    @ApiResponses({
            @ApiResponse(code = 20000, message = "表示邮箱未被注册"),
            @ApiResponse(code = 40000, message = "表示邮箱已被注册")
    })
    @CheckRepeatCommit
    @PostMapping("/email")
    public ResponseResult checkEmail(String email) {
        return userService.checkEmail(email);
    }


    /**
     * 修改用户邮箱
     * 必须是出于登录状态下
     *
     * @param email
     * @param verifyCode
     * @return
     */
    @PutMapping("/email")
    public ResponseResult updateEmail(@RequestParam("email") String email, @RequestParam("verifyCode") String verifyCode) {
        return userService.updateEmail(email, verifyCode);
    }

    /**
     * 退出登录
     *
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/logout")
    public ResponseResult doLogout(HttpServletRequest request, HttpServletResponse response) {
        return userService.doLogout(request, response);
    }

    /**
     * 获取登录二维码
     *
     * @return
     */
    @GetMapping("/qr-code-login")
    public ResponseResult getQrCodeLogin() {
        return userService.getQrCodeLogin();
    }

    /**
     * 检查二维码的登录状态
     *
     * @return
     */
    @GetMapping("/qr-code-state/{loginId}")
    public ResponseResult checkQrCodeLoginState(@PathVariable("loginId") String loginId) {
        return userService.checkQrCodeLoginState(loginId);
    }

    /**
     * 更新二维码的登录状态
     *
     * @param loginId
     * @return
     */
    @PutMapping("/qr-code-state/{loginId}")
    public ResponseResult updateQrCodeLoginState(@PathVariable("loginId") String loginId) {
        return userService.updateQrCodeLoginState(loginId);
    }

}
