package cn.codeprobe.blog.controller;

import cn.codeprobe.blog.utils.ip.IpToAddrUtil;
import cn.codeprobe.blog.utils.ip.IpUtil;
import cn.codeprobe.blog.utils.send.SendMailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/test")
public class TextController {

    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/getDomain", method = RequestMethod.GET)
    public Map<String, Object> getDomain() {
        Map<String, Object> result = new HashMap<>();
        result.put("remoteAddr", request.getRemoteAddr());
        result.put("domain", request.getServerName());
        result.put("url", request.getRequestURL());
        result.put("path", request.getServletPath());
        return result;
    }

    @GetMapping("/verify")
    public boolean verify(@RequestParam("phone") java.lang.String phone) {
        java.lang.String regex = "^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$";
        if (phone.length() != 11) {
            return false;
        } else {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(phone);
            boolean isMatch = m.matches();
            return isMatch;
        }
    }

//    @Autowired
//    private MailSenderUtil mailSenderUtil;
//
//    @GetMapping("/mail1")
//    public void sendMail() {
//        mailSenderUtil.sendRegisterVerifyCode("123123","1294689451@qq.com");
//    }

    @Autowired
    private SendMailUtil sendMailUtil;

    @GetMapping("/mail/one")
    public void sendMail1() {
        sendMailUtil.sendVerifyCode("123123", "1294689451@qq.com");
    }

    @GetMapping("/mail/one/attach")
    public void sendMail2() {

        sendMailUtil.sendVerifyCode("123123", "1294689451@qq.com");

    }

    @GetMapping("/mail/one/attaches")
    public void sendMail3() {
        String[] more = {"1294689451@qq.com", "1308753047@qq.com"};
        String[] paths = {"C:\\Users\\Lionido\\Pictures\\tyty.png", "C:\\Users\\Lionido\\Pictures\\Snipaste_2020-03-16_10-19-24.png"};
        sendMailUtil.sendAttaches("测试", "123123", more, paths);
    }


    @RequestMapping("/login/{ip}")
    public void login(HttpServletRequest request, Device device, @PathVariable("ip") String ip) {
        String ipAddress = IpUtil.getIpAddr(request);
        if (device.isMobile()) {
            System.out.println("========请求来源设备是手机！========");
        } else if (device.isTablet()) {
            System.out.println("========请求来源设备是平板！========");
        } else if (device.isNormal()) {
            System.out.println("========请求来源设备是PC！========");
        } else {
            System.out.println("========请求来源设备是其它！========");
        }
        System.out.println(device.getDevicePlatform());
        String cityInfo = IpToAddrUtil.getCityInfo(ip);
        System.out.println(ipAddress + "==>" + cityInfo);
    }

    @GetMapping("/header")
    public void getRequestHeader(HttpServletRequest request) {
        System.out.println(request.getHeader("x-forwarded-for"));
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String value = request.getHeader(headerName);
            System.out.println(headerNames.toString() + "==>" + value);
        }
    }

}
