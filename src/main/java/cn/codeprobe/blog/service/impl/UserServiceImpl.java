package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.RefreshTokenDao;
import cn.codeprobe.blog.dao.SettingDao;
import cn.codeprobe.blog.dao.UserDao;
import cn.codeprobe.blog.dao.UserNoPasswordDao;
import cn.codeprobe.blog.pojo.RefreshToken;
import cn.codeprobe.blog.pojo.Setting;
import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.pojo.UserNoPassword;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.UserService;
import cn.codeprobe.blog.service.supplementary.AsyncService;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.supplementary.CountDownLatchManager;
import cn.codeprobe.blog.utils.Jwt.ClaimsUtil;
import cn.codeprobe.blog.utils.Jwt.CookieUtil;
import cn.codeprobe.blog.utils.Jwt.JwtUtil;
import cn.codeprobe.blog.utils.common.*;
import cn.codeprobe.blog.constatnts.Constants;
import cn.codeprobe.blog.utils.id.SnowflakeIdWorker;
import cn.codeprobe.blog.utils.send.SendSmsUtil;
import cn.codeprobe.blog.utils.format.ValidateUtil;
import com.google.gson.Gson;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class UserServiceImpl extends BaseService implements UserService {

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;  // 雪花id 生成工具
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserNoPasswordDao userNoPasswordDao;
    @Autowired
    private SettingDao settingDao;
    @Autowired
    private RefreshTokenDao refreshTokenDao;
    @Autowired
    private RedisUtil redisUtil;  // redisUtil 工具类
    @Autowired
    private AsyncService asyncService; //异步发送邮件
    @Autowired
    private CountDownLatchManager countDownLatchManager;

    /**
     * json <==> object
     */
    @Autowired
    private Gson gson;

    /**
     * 初始化管理员账户
     *
     * @param user
     * @param request
     * @return
     */
    @Override
    public ResponseResult initManagerAccount(User user, HttpServletRequest request) {

        //检查是否已经初始化
        Setting settingUser = settingDao.findOneByKey(Constants.Settings.INIT_MANAGER_ACCOUNT);
        if (settingUser != null) {
            return ResponseResult.FAILED("管理员已经初始化过了");
        }
        //检查数据
        if (StringUtil.isEmpty(user.getUsername())) {
            return ResponseResult.FAILED("用户名不可以为空！");
        }
        if (StringUtil.isEmpty(user.getPassword())) {
            return ResponseResult.FAILED("密码不可以为空！");
        }
        if (StringUtil.isEmpty(user.getEmail())) {
            return ResponseResult.FAILED("邮箱不可以为空！");
        }
        //补充数据
        user.setId(snowflakeIdWorker.nextId() + "");
        log.info("id ==> " + user.getId());
        user.setRoles(Constants.User.ROLE_ADMIN);
        if (StringUtil.isEmpty(user.getAvatar())) {
            user.setAvatar(Constants.User.DEFAULT_AVATAR);
        }
        if (StringUtil.isEmpty(user.getSign())) {
            user.setSign(Constants.User.DEFAULT_SIGN);
        }
        user.setState(Constants.User.DEFAULT_ADMIN_STATE);
        user.setReg_ip(request.getRemoteAddr());
        user.setLogin_ip(request.getLocalAddr());
        log.info("reg_ip == >" + user.getReg_ip());
        log.info("login_ip == >" + user.getLogin_ip());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        //加密密码
        String enPassword = StringUtil.enPassword(user.getPassword());
        log.info("enPassword ==> " + enPassword);
        user.setPassword(enPassword);
        //保存到数据库
        userDao.save(user);
        //保存更新标记
        Setting setting = new Setting();
        setting.setId(snowflakeIdWorker.nextId() + "");
        setting.setKey(Constants.Settings.INIT_MANAGER_ACCOUNT);
        setting.setValue(Constants.Settings.INIT_MANAGER_ACCOUNT_VALUE);
        setting.setCreateTime(new Date());
        setting.setUpdateTime(new Date());
        settingDao.save(setting);
        return ResponseResult.SUCCESS("管理员初始化成功");
    }

    /**
     * 生成图灵验证码
     *
     * @param request
     * @param response
     * @param captchaKey
     */
    @Override
    public void createCaptcha(HttpServletRequest request, HttpServletResponse response, String captchaKey) throws Exception {
        // 判断key是否符合时间戳的长度
        if (StringUtil.isEmpty(captchaKey) || captchaKey.length() < 13) {
            return;
        }
        long key = Long.parseLong(captchaKey);

        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        Random random = new Random();
        // 验证码类型
        int CaptchaType = random.nextInt(3);
        Captcha captcha = null;
        if (CaptchaType == 1) {
            // png类型
            captcha = new SpecCaptcha(200, 50, 5);
        } else if (CaptchaType == 2) {
            // gif类型
            captcha = new GifCaptcha(200, 50);
        } else {
            // 算术类型
            captcha = new ArithmeticCaptcha(200, 50);
            captcha.setLen(3);  // 几位数运算，默认是两位
        }

        // 设置字体，通过数组随机选择字体类型
        int[] captcha_font_type = {
                Captcha.FONT_1,
                Captcha.FONT_2,
                Captcha.FONT_3,
                Captcha.FONT_4,
                Captcha.FONT_5,
                Captcha.FONT_6,
                Captcha.FONT_7,
                Captcha.FONT_8,
                Captcha.FONT_9,
                Captcha.FONT_10
        };
        int randomType = random.nextInt(captcha_font_type.length);
        log.info("captcha_font_type ==> " + randomType);
        captcha.setFont(captcha_font_type[randomType]);

        // 设置类型，纯数字、纯字母、字母数字混合
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        // 将生成的验证码转换为小写
        String content = captcha.text().toLowerCase();
        log.info("captcha content ==> " + content);
        // 验证码存入 redis
        redisUtil.set(Constants.User.KEY_CAPTCHA_CODE + key, content, 10 * Constants.TimeValueInSeconds.MIN);
        // 输出图片流
        captcha.out(response.getOutputStream());
    }


    /**
     * 发送邮箱验证码
     * <p>
     * 使用场景: 注册、找回密码、更换邮箱
     * 注册: 如果已经注册过了，就提示该邮箱已经被注册
     * 找回密码: 如果没有注册过，提示该邮箱没有注册
     * 修改邮箱: 如果已经注册了，就提示改邮箱已经被注册
     *
     * <p>
     * type:
     * register(注册):
     * update(修改密码):
     * forget(找回密码):
     *
     * @param request
     * @param emailAddr
     * @return
     */
    @Override
    public ResponseResult sendVerifyCode(HttpServletRequest request, String type, String emailAddr) {
        if (emailAddr == null) {
            return ResponseResult.FAILED("邮箱地址不可以为空");
        }
        // 根据类型查询邮箱是否存在
        User oneByEmail = userDao.findOneByEmail(emailAddr);
        if (type.equals("register") || type.equals("update")) {
            if (oneByEmail != null) {
                return ResponseResult.FAILED("该邮箱已被注册");
            }
        } else if (type.equals("forget")) {
            if (oneByEmail == null) {
                return ResponseResult.FAILED("该邮箱没有注册");
            }
        }
        String remoteAddr = request.getRemoteAddr();
        String replace = remoteAddr.replace(":", "_");
        log.info("remote emailAddr ==> " + replace);
        // 防止暴力发送，限制每个IP一个小时之内只能发送10次验证码，同一个邮箱地址，每隔30秒的才能重新发送
        String sendStats = (String) redisUtil.get(Constants.User.KEY_EMAIL_ADDR_SEND_CODE + emailAddr);
        String ipSendTimesStr = (String) redisUtil.get(Constants.User.KEY_EMAIL_IP_SEND_CODE + replace);
        Integer ipSendTimes = -1;
        if (!StringUtil.isEmpty(ipSendTimesStr)) {
            ipSendTimes = Integer.parseInt(ipSendTimesStr);
        }
        if (ipSendTimesStr != null && ipSendTimes >= 10) {
            return ResponseResult.FAILED("您点击发送验证码的次数过于频繁，请一个小时后再次尝试");
        }
        if (sendStats != null) {
            return ResponseResult.FAILED("您点击发送验证码过于频繁，请稍后再试");
        }
        // 检验邮箱格式是否正确
        Boolean emailFormatOK = ValidateUtil.isEmailFormatOK(emailAddr);
        if (!emailFormatOK) {
            return ResponseResult.FAILED("您输入的邮箱地址有误，请重新输入");
        }
        // 生成随机六位数验证码
        Integer code = (int) ((Math.random() * 9 + 1) * 100000);
        // 异步发送验证码，解决发送邮件，需要等待的时间过长
        try {
            asyncService.sendEmailVerifyCode(code.toString(), emailAddr);
        } catch (Exception e) {
            return ResponseResult.FAILED("邮箱验证码发送失败,请稍后重试");
        }
        // 发送记录
        if (ipSendTimesStr == null) {
            ipSendTimes = 0;
        }
        ipSendTimes++;
        redisUtil.set(Constants.User.KEY_EMAIL_IP_SEND_CODE + replace, ipSendTimes.toString(), Constants.TimeValueInSeconds.HOUR);
        redisUtil.set(Constants.User.KEY_EMAIL_ADDR_SEND_CODE + emailAddr, "hasSend", Constants.TimeValueInSeconds.MIN);
        // 保存code
        redisUtil.set(Constants.User.KEY_EMAIL_CODE + emailAddr, code.toString(), 30 * Constants.TimeValueInSeconds.MIN);
        log.info(" 验证码" + code + "已被保存到redis中");
        return ResponseResult.SUCCESS("邮箱验证码发送成功");
    }

    @Autowired
    private SendSmsUtil sendSmsUtil;

    /**
     * 连接阿里云短信服务，发送短信验证码
     *
     * @param phoneNumbers
     * @return
     */
    @Override
    public ResponseResult sendSms(String phoneNumbers) {
        //检查手机格式是否正确
        if (!ValidateUtil.isPhone(phoneNumbers)) {
            return ResponseResult.FAILED("请输入正确的手机号");
        }
        //防止重复发送，因为短信是要花钱滴
        String status = (String) redisUtil.get(Constants.User.KEY_SMS_CODE + phoneNumbers);
        if (!StringUtil.isEmpty(status)) {
            return ResponseResult.SUCCESS("验证码已发送至您的手机");
        }
        //生成随机六位数verify_code
        String verify_code = String.valueOf(Math.random()).substring(2, 8);
        Map<String, Object> code = new HashMap<>();
        code.put("code", verify_code);
        log.info("手机验证码 ==> " + verify_code.toString() + "以发送至" + phoneNumbers);
        //发送短信验证码
        boolean isSuccess = sendSmsUtil.sendSms(code);
        //发送成功，将verify_code放入redis缓存
        if (isSuccess) {
            redisUtil.set(Constants.User.KEY_SMS_CODE + phoneNumbers, verify_code, Constants.TimeValueInSeconds.MIN * 5);
            //发送成功
            return ResponseResult.SUCCESS("验证码发送成功");
        }
        //发送失败
        return ResponseResult.SUCCESS("验证码发送失败");
    }

    /**
     * 普通用户注册
     *
     * @param user
     * @param type
     * @param verifyCode
     * @param captchaCode
     * @param captchaKey
     * @param request
     * @return
     */
    @Override
    public ResponseResult register(User user, String type, String verifyCode, String captchaCode, String captchaKey, HttpServletRequest request) {
        // 第一步：检查当前用户名是否已经注册
        User oneByUsername = userDao.findOneByUsername(user.getUsername());
        if (oneByUsername != null) {
            return ResponseResult.FAILED("该用户名已被注册");
        }
        //如果是通过邮箱注册，需要的检测步骤
        if (Constants.User.REGISTER_BY_EMAIL.equals(type)) {
            ResponseResult result = verifyByEmail(user, verifyCode);
            if (result != null) return result;
        } else {
            ResponseResult result = verifyByPhone(user, verifyCode);
            if (result != null) return result;
        }
        //检查图灵验证码是否正确
        long key = Long.parseLong(captchaKey);
        String redisCaptchaCode = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CODE + key);
        if (redisCaptchaCode == null) {
            return ResponseResult.FAILED("人类验证码已失效");
        }
        if (!redisCaptchaCode.equals(captchaCode)) {
            return ResponseResult.FAILED("人类验证码有误");
        }
        // 用户输入的图灵验证码正确，删除redis里的图灵验证码，保证一次性，不可以二次注册
        redisUtil.del(Constants.User.KEY_CAPTCHA_CODE + key);
        // 达到可以注册的条件
        // 对密码进行加密
        if (user.getPassword() == null) {
            return ResponseResult.FAILED("密码不可以为空");
        }
        String enPassword = StringUtil.enPassword(user.getPassword());
        //补全数据
        //包括：注册IP,登录IP,角色,头像,创建时间,更新时间
        user.setId(snowflakeIdWorker.nextId() + "");
        user.setPassword(enPassword);
        user.setRoles(Constants.User.ROLE_NORMAL);
        user.setAvatar(Constants.User.DEFAULT_AVATAR);
        user.setSign(Constants.User.DEFAULT_SIGN);
        user.setState(Constants.User.DEFAULT_NORMAL_STATE);
        user.setReg_ip(request.getRemoteAddr());
        user.setLogin_ip(request.getRemoteAddr());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        // 保存到数据库中
        userDao.save(user);
        // 用户注册成功后，删除redis里的邮箱验证码，保证一次性，不可以二次注册
        if (Constants.User.REGISTER_BY_EMAIL.equals(type)) {
            redisUtil.del(Constants.User.KEY_EMAIL_CODE + user.getEmail());
        } else {
            redisUtil.del(Constants.User.KEY_SMS_CODE + user.getPhone());
        }
        // 返回结果
        return ResponseResult.REGISTER_SUCCESS();
    }

    /**
     * 通过手机注册
     *
     * @param user
     * @param verifyCode
     * @return
     */
    private ResponseResult verifyByPhone(User user, String verifyCode) {
        if (StringUtil.isEmpty(user.getPhone())) {
            return ResponseResult.FAILED("手机号不可以为空");
        }
        if (!ValidateUtil.isPhone(user.getPhone())) {
            return ResponseResult.FAILED("请输入正确的手机号");
        }
        String redisPhoneCode = (String) redisUtil.get(Constants.User.KEY_SMS_CODE + user.getPhone());
        if (StringUtil.isEmpty(redisPhoneCode)) {
            return ResponseResult.FAILED("手机验证码已过期");
        }
        if (!redisPhoneCode.equals(verifyCode)) {
            return ResponseResult.FAILED("您输入的验证码验证码有误");
        }
        return null;
    }

    /**
     * 通过邮箱注册
     *
     * @param user
     * @param verifyCode
     * @return
     */
    private ResponseResult verifyByEmail(User user, String verifyCode) {
        //检查邮箱格式是否正确
        Boolean emailFormatOK = ValidateUtil.isEmailFormatOK(user.getEmail());
        if (!emailFormatOK) {
            return ResponseResult.FAILED("邮箱地址格式有误");
        }
        //检查该邮箱是否已经注册
        User oneByEmail = userDao.findOneByEmail(user.getEmail());
        if (oneByEmail != null) {
            return ResponseResult.FAILED("该邮箱已被占用");
        }
        //检查邮箱验证码是否正确
        String redisEmailCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE + user.getEmail());
        if (StringUtil.isEmpty(redisEmailCode)) {
            return ResponseResult.FAILED("邮箱验证码已过期");
        }
        if (!redisEmailCode.equals(verifyCode)) {
            return ResponseResult.FAILED("您输入的邮箱验证码有误");
        }
        return null;
    }

    /**
     * 多端、单点登录（移动，PC）
     * double token 的思想实现
     *
     * <p>
     * Token是服务端生成的一串字符串，以作客户端进行请求的一个令牌，
     * 当第一次登录后，服务器生成一个Token便将此Token的MD5加密结果tokenKey通过Cookie返回给客户端有效期为半年，
     * 同时将Token 存入redis中，有效期为2小时,tokenKey 即为Token 的Key
     * 以后客户端只需带上这个tokenKey前来请求数据即可，无需再次带上用户名和密码。
     * 但是tokenKy的有效时间仅为两小时，过了两小时，用户便无法通过tokenKey来验证登录，
     * 这里有一个问题，如何不让用户重新输入用户名、密码登录，而用tokenKey再次登录。
     * 这就需要 refreshToken
     *
     * <p>
     * refreshToken 存入数据库中，有效期为一个月，通过它可以使用户的令牌tokenKey再次生效
     * 从而不需要通过登录界面再次登录。当有效期过了，则需要用户输入用户名、密码重新登陆
     *
     * <p>
     * 单点登录
     * 保证同一个终端，只有一台设备在线，即 redis中 token唯一
     *
     * @param captcha    图灵验证码
     * @param captchaKey 图灵验证码key
     * @param user       登录用户
     * @param terminal   登陆终端
     * @param request
     * @param response
     * @return
     */
    @Override
    public ResponseResult doLogin(String captcha, String captchaKey, User user, String terminal, HttpServletRequest request, HttpServletResponse response) {
        //验证图灵验证码是否正确，是否是机器人
        String redisCaptchaCode = (String) redisUtil.get(Constants.User.KEY_CAPTCHA_CODE + captchaKey);
        if (redisCaptchaCode == null) {
            return ResponseResult.FAILED("人类验证码已过期");
        }
        if (!redisCaptchaCode.equals(captcha)) {
            return ResponseResult.FAILED("人类验证码错误");
        }
        //验证成功，删除redis里的验证码,释放内存
        redisUtil.del(Constants.User.KEY_CAPTCHA_CODE + captchaKey);
        //检查数据
        if (StringUtil.isEmpty(user.getUsername())) {
            if (StringUtil.isEmpty(user.getEmail())) {
                if (StringUtil.isEmpty(user.getPhone())) {
                    return ResponseResult.FAILED("账户名称不可以为空");
                }
            }
        }
        if (StringUtil.isEmpty(user.getPassword())) {
            return ResponseResult.FAILED("密码不可以为空");
        }
        //检查当前登录用户是否存在
        User dbUser = userDao.findOneByUsername(user.getUsername());
        if (dbUser == null) {
            dbUser = userDao.findOneByEmail(user.getUsername());
            if (dbUser == null) {
                dbUser = userDao.findOneByPhone(user.getUsername());
                if (dbUser == null) {
                    return ResponseResult.FAILED("用户名或密码不正确");
                }
            }
        }
        //检查该用户是否具备登陆权限
        if (!dbUser.getState().equals("1")) {
            return ResponseResult.FAILED("当前帐号已被禁止登录");
        }
        //用户存在，检查密码是否正确
        boolean isPasswordOk = StringUtil.matchPassword(user.getPassword(), dbUser.getPassword());
        if (!isPasswordOk) {
            return ResponseResult.FAILED("用户名或密码不正确");
        }
        //检查登陆终端
        if (StringUtil.isEmpty(terminal) || !Constants.Terminal.TERMINAL_PC.equals(terminal)) {
            terminal = Constants.Terminal.TERMINAL_MOBILE;
        } else if (Constants.Terminal.TERMINAL_PC.equals(terminal)) {
            terminal = Constants.Terminal.TERMINAL_PC;
        }
        //用户登录条件检查完成，可以登录

        //创建token & refreshToken
        createTokenAndRefreshToken(request, response, dbUser, terminal);
        //返回登录结果
        return ResponseResult.LOGIN_SUCCESS();
    }

    /**
     * 创建 token、tokenKey、refreshTokenKey
     *
     * @param request
     * @param response
     * @param dbUser
     * @param terminal
     * @return
     */
    private String createTokenAndRefreshToken(HttpServletRequest request, HttpServletResponse response, User dbUser, String terminal) {
        // 单点登录，保证同一终端的token唯一，即只有一个设备在线
        if (Constants.Terminal.TERMINAL_MOBILE.equals(terminal)) {
            String dbMobileTokenKey = refreshTokenDao.findMobileTokenKeyById(dbUser.getId());
            log.info("dbMobileTokenKey ==> " + dbMobileTokenKey);
            redisUtil.del(Constants.User.KEY_TOKEN + dbMobileTokenKey);
        } else {
            String dbTokenKey = refreshTokenDao.findTokenKeyById(dbUser.getId());
            log.info("dbTokenKey ==> " + dbTokenKey);
            redisUtil.del(Constants.User.KEY_TOKEN + dbTokenKey);
        }
        // 删除old的token，保证同一设备的token唯一
        String oldTokenKey = CookieUtil.getCookieValue(request, Constants.Cookie.COOKIE_TOKEN_KEY);
        redisUtil.del(Constants.User.KEY_TOKEN + oldTokenKey);
        // 生成token
        Map<String, Object> claims = ClaimsUtil.User2Claims(dbUser, terminal);
        String token = JwtUtil.createToken(claims, Constants.TimeValueInMillions.HOUR_2);
        log.info("token ==> " + token);
        // 生成tokenKey
        String tokenKey = DigestUtils.md5DigestAsHex(token.getBytes());
        log.info("tokenKey ==> " + tokenKey);
        // 将token保存在redis,有效期位两小时,tokenKey作为key
        redisUtil.set(Constants.User.KEY_TOKEN + tokenKey, token, Constants.TimeValueInSeconds.HOUR_2);
        // 使用CookiesUtil 工具类将tokenKey通过Cookies返回给客户端
        CookieUtil.setupCookie(response, Constants.Cookie.COOKIE_TOKEN_KEY, tokenKey);
        // 创建或更新refreshTokenKey
        RefreshToken refreshToken = refreshTokenDao.findOneByUserId(dbUser.getId());
        // 创建
        if (refreshToken == null) {
            refreshToken = new RefreshToken();
            refreshToken.setId(snowflakeIdWorker.nextId() + "");
            refreshToken.setUserId(dbUser.getId());
            refreshToken.setCreateTime(new Date());
            String refreshTokenValue = JwtUtil.createRefreshToken(dbUser.getId(), Constants.TimeValueInMillions.MONTH);
            refreshToken.setRefreshToken(refreshTokenValue);
        }
        // 对应终端的tokenKey保存或更新到refreshToken
        if (Constants.Terminal.TERMINAL_MOBILE.equals(terminal)) {
            refreshToken.setMobileTokenKey(tokenKey);
        } else {
            refreshToken.setTokenKey(tokenKey);
        }
        refreshToken.setUpdateTime(new Date());
        refreshTokenDao.save(refreshToken);
        return tokenKey;
    }


    /**
     * 检查用户的登陆状态
     *
     * @return
     */
    @Override
    public User checkLoginStatus() {
        // 获取request,response
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        // 判断当前要提交的用户是谁，也就是判断该用户是否登录
        String tokenKey = CookieUtil.getCookieValue(request, Constants.Cookie.COOKIE_TOKEN_KEY);
        if (tokenKey == null) {
            return null;
        }
        User user = parseByTokenKey2User(tokenKey);
        String terminal = parseByTokenKey2Terminal(tokenKey);
        log.info("terminal ==> " + terminal);
        // 当前没有用户的登录状态，从mysql中查询refreshToken
        if (user == null) {
            //通过refreshToken 来进一步判断是用户需要登录，还是token过期了
            RefreshToken dbRefreshToken = null;
//            if (Constants.Terminal.TERMINAL_MOBILE.equals(terminal)) {
//                dbRefreshToken = refreshTokenDao.findOneByTokenKey(tokenKey);
//            } else {
//                dbRefreshToken = refreshTokenDao.findOneByMobileTokenKey(tokenKey);
//            }
            dbRefreshToken = refreshTokenDao.findOneByTokenKey(tokenKey);
            if (dbRefreshToken != null) {
                terminal = Constants.Terminal.TERMINAL_PC;
            } else {
                dbRefreshToken = refreshTokenDao.findOneByMobileTokenKey(tokenKey);
                terminal = Constants.Terminal.TERMINAL_MOBILE;
            }
            if (dbRefreshToken == null) {
                // 需要登录
                return null;
            } else {
                //token已经过期,重新创建token和refreshToken，并删除原来的token
                //从refreshTokenKey中解析出用户ID
                String refreshToken = dbRefreshToken.getRefreshToken();
                Claims claims = null;
                try {
                    claims = JwtUtil.parseJWT(refreshToken);
                } catch (Exception e) {
                    //refreshTokenKey过期，删除数据库记录，用户重新登陆
                    log.info("refreshTokenKey过期");
                    refreshTokenDao.deleteOneByTokenKey(tokenKey);
                    return null;
                }
                String userId = claims.getId();
                log.info("refreshToken 解析出来的userId ==>" + userId);
                User dbUser = userDao.findOneById(userId);
                // 检查该用户是否具备登陆权限
                if (!dbUser.getState().equals("1")) {
                    return null;
                }
                String newTokenKey = createTokenAndRefreshToken(request, response, dbUser, terminal);
                return parseByTokenKey2User(newTokenKey);
            }
        }
        return user;
    }

    /**
     * 通过 tokenKey 获取用户的登录状态
     *
     * @param tokenKey
     * @return
     */
    @Override
    public User parseByTokenKey2User(String tokenKey) {
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        if (token != null) {
            try {
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtil.Claims2User(claims);
            } catch (Exception e) {
                // 解析时token已经过期
                return null;
            }
        }
        return null;
    }

    /**
     * 通过 tokenKey 获取用户登陆终端
     *
     * @param tokenKey
     * @return
     */
    public String parseByTokenKey2Terminal(String tokenKey) {
        String token = (String) redisUtil.get(Constants.User.KEY_TOKEN + tokenKey);
        if (token != null) {
            try {
                Claims claims = JwtUtil.parseJWT(token);
                return ClaimsUtil.Claims2Terminal(claims);
            } catch (Exception e) {
                // 解析时token已经过期
                return null;
            }
        }
        return null;
    }


    @Override
    public ResponseResult getUserInfo(String userId) {
        User user = userDao.findOneById(userId);
        if (user == null) {
            return ResponseResult.FAILED("用户不存在");
        }
//        不可以这样将从数据库中的密码置空，因为此时事务还没提交，一旦这样设置，数据库用户密码，就真的没有了
//        user.setPassword("");
        //通过Gson来复制对象，将不想展示给客户端的敏感信息通过置空的方式屏蔽，当然也可以查询部分内容
        String userJson = gson.toJson(user);
        User copyUser = gson.fromJson(userJson, User.class);
        copyUser.setPassword("");
        copyUser.setReg_ip("");
        copyUser.setEmail("");
        copyUser.setLogin_ip("");
        return ResponseResult.SUCCESS("获取用户信息成功").setData(copyUser);
    }

    /**
     * 检查用户名是否被注册
     *
     * @param username
     * @return
     */
    @Override
    public ResponseResult checkUsername(String username) {
        User oneByUsername = userDao.findOneByUsername(username);
        return oneByUsername == null ? ResponseResult.SUCCESS("用户名未被占用") : ResponseResult.FAILED("该用户名已被占用");
    }

    /**
     * 检查邮箱是否已被注册
     *
     * @param email
     * @return
     */
    @Override
    public ResponseResult checkEmail(String email) {
        User oneByEmail = userDao.findOneByEmail(email);
        return oneByEmail == null ? ResponseResult.SUCCESS("该邮箱可以使用") : ResponseResult.FAILED("该邮箱已被注册");
    }

    /**
     * 修改用户信息
     * <p>
     * 允许哟用户修改的内容：
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
    @Override
    public ResponseResult updateUserInfo(String userId, User user, HttpServletRequest request) {
        // 检查登录信息
        User userLogin = checkLoginStatus();
        if (userLogin == null) {
            return ResponseResult.NOT_LOGIN();
        }
        // 登录用户只能修改自己的信息
        if (!userLogin.getId().equals(userId)) {
            return ResponseResult.PERMISSION_DENIED();
        }
        User dbUser = userDao.findOneById(userLogin.getId());
        // 头像
        dbUser.setAvatar(user.getAvatar());
        // 用户名
        dbUser.setUsername(user.getUsername());
        // 签名
        dbUser.setSign(user.getSign());
        dbUser.setUpdateTime(new Date());
        // 保存修改信息
        userDao.save(dbUser);
        // 更新redis 里的token
        String tokenKey = CookieUtil.getCookieValue(request, Constants.Cookie.COOKIE_TOKEN_KEY);
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        // 返回修改结果
        return ResponseResult.SUCCESS("用户信息修改成功");
    }

    /**
     * 删除用户
     *
     * <p>
     * 需要管理员权限
     * 通过将 state ==> 0 删除用户，并不从数据库中抹除
     * </p>
     *
     * @param userId
     * @return
     */
    @Override
    public ResponseResult deleteUser(String userId) {
//        // 判断当前登录用户
//        User user = checkLoginStatus(response, request);
//        if (user == null) {
//            return ResponseResult.NOT_LOGIN();
//        }
//        // 是否有权限删除
//        if (!user.getRoles().equals(Constants.User.ROLE_ADMIN)) {
//            return ResponseResult.PERMISSION_DENIED();
//        }

        // 删除
        int result = userDao.deleteByState(userId);
        if (result > 0) {
            return ResponseResult.SUCCESS("删除成功");
        }
        return ResponseResult.FAILED("删除失败");
    }

    /**
     * 获取用户列表
     * <p>
     * 需要管理员权限
     * </P>
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listUsers(int page, int size) {
//        // 判断当前登录用户
//        User user = checkLoginStatus(response, request);
//        if (user == null) {
//            return ResponseResult.NOT_LOGIN();
//        }
//        // 是否有管理员权限，只有管理员才可以获取用户列表
//        if (!user.getRoles().equals(Constants.User.ROLE_ADMIN)) {
//            return ResponseResult.PERMISSION_DENIED();
//        }
        // 限制最小分页，和最小每页数量
        page = checkPage(page);
        size = checkSize(size);
        // 根据注册日期来排序，分页查询
        Sort sortByCreateTime = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sortByCreateTime);
        Page<UserNoPassword> all = userNoPasswordDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取用户列表成功").setData(all);
    }

    /**
     * 更换邮箱密码
     *
     * @param user
     * @param verifyCode
     * @return
     */
    @Override
    public ResponseResult updatePassword(User user, String verifyCode) {
        // 检查邮箱是否填写
        if (user.getEmail() == null) {
            return ResponseResult.FAILED("邮箱不可以为空");
        }
        // 检查密码是否填写
        if (user.getPassword() == null) {
            return ResponseResult.FAILED("密码不可以为空");
        }
        // 从redis里去拿验证码比较是否正确
        String redisVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE + user.getEmail());
        if (redisVerifyCode == null || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("邮箱验证码无效");
        }
        // 验证成功后释放缓存
        redisUtil.del(Constants.User.KEY_EMAIL_CODE + user.getEmail());
        // 更新密码
        int result = userDao.updatePassword(StringUtil.enPassword(user.getPassword()), user.getEmail());
        return result > 0 ? ResponseResult.SUCCESS("密码修改成功") : ResponseResult.FAILED("密码修改失败");
    }


    /**
     * 更换邮箱
     *
     * @param emailAddr
     * @param verifyCode
     * @return
     */
    @Override
    public ResponseResult updateEmail(String emailAddr, String verifyCode) {
        // 检验数据是否为空
        if (emailAddr == null) {
            return ResponseResult.FAILED("邮箱地址不可以为空");
        }
        if (verifyCode == null) {
            return ResponseResult.FAILED("邮箱验证码不可以为空");
        }
        // 判断是否登录
        User userLogin = checkLoginStatus();
        if (userLogin == null) {
            return ResponseResult.NOT_LOGIN();
        }
        // 从redis拿到验证码比较
        String redisVerifyCode = (String) redisUtil.get(Constants.User.KEY_EMAIL_CODE + emailAddr);
        if (redisVerifyCode == null || !redisVerifyCode.equals(verifyCode)) {
            return ResponseResult.FAILED("邮箱验证码不正确");
        }
        // 验证成功后释放缓存
        redisUtil.del(Constants.User.KEY_EMAIL_CODE + emailAddr);
        // 修改邮箱
        int result = userDao.updateEmail(emailAddr, userLogin.getId());
        return result > 0 ? ResponseResult.SUCCESS("邮箱修改成功") : ResponseResult.FAILED("邮箱修改失败");
    }


    /**
     * <p>
     * 1. 删除tokenKey
     * 2. 删除redis里的token
     * 3. 删除mysql里的refreshToken
     * </p>
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    public ResponseResult doLogout(HttpServletRequest request, HttpServletResponse response) {
        // 获取tokenKey
        String tokenKey = CookieUtil.getCookieValue(request, Constants.Cookie.COOKIE_TOKEN_KEY);
        if (tokenKey == null) {
            return ResponseResult.NOT_LOGIN();
        }
        // 删除mysql中的tokenKey
        User currentUser = parseByTokenKey2User(tokenKey);
        String terminal = parseByTokenKey2Terminal(tokenKey);
        int result;
        if (Constants.Terminal.TERMINAL_MOBILE.equals(terminal)) {
            result = refreshTokenDao.deleteMobileTokenKey(currentUser.getId());
        } else {
            result = refreshTokenDao.deleteTokenKey(currentUser.getId());
        }
        log.info("logout result ==> " + result);
        // 删除token
        redisUtil.del(Constants.User.KEY_TOKEN + tokenKey);
        // 删除客户端Cookie里的Cookie
        CookieUtil.delCookie(response, Constants.Cookie.COOKIE_TOKEN_KEY, tokenKey);
        return result > 0 ? ResponseResult.SUCCESS("退出登陆成功") : ResponseResult.SUCCESS("退出登陆失败");
    }

    /**
     * 获取登陆二维码
     * 防止频繁获取二维码
     *
     * @return
     */
    @Override
    public ResponseResult getQrCodeLogin() {
        String isAcquired = (String) redisUtil.get(Constants.User.KEY_REPEAT_ACQUIRE_QR_CODE);
        if (!StringUtil.isEmpty(isAcquired)) {
            return ResponseResult.FAILED("连续请求次数过多，服务器繁忙，请稍后重试");
        }
        String qrcLoginId = CookieUtil.getCookieValue(getRequest(), Constants.Cookie.COOKIE_QR_CODE);
        if (!StringUtil.isEmpty(qrcLoginId)) {
            redisUtil.del(Constants.User.KEY_QR_CODE + qrcLoginId);
        }
        String loginId = snowflakeIdWorker.nextId() + "";
        String url = getDomain() + "/portal/image/qr-code/" + loginId;
        Map<String, Object> result = new HashMap<>();
        result.put("code", loginId);
        result.put("url", url);
        redisUtil.set(Constants.User.KEY_QR_CODE + loginId, Constants.User.QR_CODE_LOGIN_FAILED, 5 * Constants.TimeValueInSeconds.MIN);

        CookieUtil.setupCookie(getResponse(), Constants.Cookie.COOKIE_QR_CODE, loginId, Constants.TimeValueInSeconds.MIN);

        redisUtil.set(Constants.User.KEY_REPEAT_ACQUIRE_QR_CODE, "has acquired", Constants.TimeValueInSeconds.MIN / 6);
        return ResponseResult.SUCCESS("获取登录二维码成功").setData(result);
    }


    private String getDomain() {
        StringBuffer requestURL = getRequest().getRequestURL();
        String servletPath = getRequest().getServletPath();
        String originalDomain = requestURL.toString().replace(servletPath, "");
        return originalDomain;
    }

    /**
     * 检查二维码的登录状态
     * 结果有：
     * 1、登录成功（loginId对应的值为有ID内容）
     * 2、等待扫描（loginId对应的值为false）
     * 3、二维码已经过期了 loginId对应的值为null
     *
     * <p>
     * 是被PC端轮询调用的
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult checkQrCodeLoginState(String loginId) {
        //获取request，response
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        //检查登录状态
        ResponseResult result = checkLoginIdState(loginId, request, response);
        if (result != null) return result;
        //先等待一段时间，再去检查
        //如果超出了这个时间，我就们就返回等待扫码
        Callable<ResponseResult> callable = new Callable<ResponseResult>() {
            @Override
            public ResponseResult call() throws Exception {
                try {
                    log.info("start waiting for scan...");
                    //先阻塞
                    countDownLatchManager.getLatch(loginId).await(Constants.User.QR_CODE_STATE_CHECK_WAITING_TIME, TimeUnit.SECONDS);
                    //收到状态更新的通知，我们就检查loginId对应的状态
                    log.info("start check login state...");
                    ResponseResult checkResult = checkLoginIdState(loginId, request, response);
                    if (checkResult != null) return checkResult;
                    //超时则返回等待扫描
                    //完事后，删除对应的latch
                    return ResponseResult.WAiTING_FOR_SCAN();
                } finally {
                    log.info("delete latch...");
                    countDownLatchManager.deleteLatch(loginId);
                }
            }
        };
        try {
            return callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.WAiTING_FOR_SCAN();
    }

    /**
     * 检查二维码登陆状态
     *
     * @param loginId
     * @param request
     * @param response
     * @return
     */
    private ResponseResult checkLoginIdState(String loginId, HttpServletRequest request, HttpServletResponse response) {
        //从redis里取值出来
        String loginState = (String) redisUtil.get(Constants.User.KEY_QR_CODE + loginId);
        //二维码过期
        if (StringUtil.isEmpty(loginState)) {
            return ResponseResult.QR_CODE_DEPRECATE();
        }
        //登陆成功
        if (!StringUtil.isEmpty(loginState) && !Constants.User.QR_CODE_LOGIN_FAILED.equals(loginState)) {
            User dbUser = userDao.findOneById(loginState);
            if (dbUser == null) {
                return ResponseResult.QR_CODE_DEPRECATE();
            }
            //创建token && refreshTokenKey
            createTokenAndRefreshToken(request, response, dbUser, Constants.Terminal.TERMINAL_PC);
            return ResponseResult.LOGIN_SUCCESS();
        }
        return null;
    }

    /**
     * 更新二维码的登录状态
     *
     * @param loginId
     * @return
     */
    @Override
    public ResponseResult updateQrCodeLoginState(String loginId) {
        //1、检查用户是否登录
        User user = checkLoginStatus();
        if (user == null) {
            return ResponseResult.NOT_LOGIN();
        }
        //2、改变loginId对应的值=true
        redisUtil.set(Constants.User.KEY_QR_CODE + loginId, user.getId());
        //2.1、通知正在等待的扫描任务
        countDownLatchManager.onPhoneDoLogin(loginId);
        //3、返回结果
        return ResponseResult.SUCCESS("登录成功.");
    }


    private HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        return request;
    }

    private HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        return response;
    }

}
