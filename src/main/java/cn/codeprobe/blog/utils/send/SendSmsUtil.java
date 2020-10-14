package cn.codeprobe.blog.utils.send;

import cn.codeprobe.blog.constatnts.Constants;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信验证码工具类
 */
@Component
public class SendSmsUtil {

    public boolean sendSms(Map<String, Object> code) {
        //连接阿里云
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI4GGEAreobT25KJeSDgpN", "VOhMMQZZ3rWJBmJaxoSWVuBNCGaYeK");
        IAcsClient client = new DefaultAcsClient(profile);

        //构建请求
        CommonRequest request = new CommonRequest();
        //固定参数
        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        //事件名称（可以自定义）
        request.setSysAction("SendSms");
        //自定义参数（手机号，验证码，签名，模板，）
        request.putQueryParameter("PhoneNumbers", Constants.User.KEY_SMS_PHONE_NUMBERS);
        request.putQueryParameter("SignName", Constants.User.KEY_SMS_SIGN_NAME);
        request.putQueryParameter("TemplateCode", Constants.User.KEY_SMS_TEMPLATE_CODE);
        request.putQueryParameter("TemplateParam ", JSONObject.toJSONString(code));
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            return true;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
        return false;
    }
}
