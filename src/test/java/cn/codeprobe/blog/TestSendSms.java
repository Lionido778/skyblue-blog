package cn.codeprobe.blog;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest
public class TestSendSms {


    @Test
    public static void main(String[] args) {
        //连接阿里云
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "L4342342342保密234N", "VOhMMQZZ3rWJBaasdafaf保密K");
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
        request.putQueryParameter("PhoneNumbers", "17789445253");
        request.putQueryParameter("SignName", "蔚蓝博客");
        request.putQueryParameter("TemplateCode", "SMS_198667747");
        //生成短信验证码
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", 671231);
        request.putQueryParameter("TemplateParam ", JSONObject.toJSONString(map));
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }
}


