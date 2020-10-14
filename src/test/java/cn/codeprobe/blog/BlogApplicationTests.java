package cn.codeprobe.blog;

import cn.codeprobe.blog.utils.ip.IpToAddrUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mobile.device.Device;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BlogApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    Device device;
    @Test
    public void contextLoads() {
        System.out.println(dataSource.getClass());
        try {
            System.out.println(dataSource.getConnection());
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }



    @Test
    public void testIptoAddr(){
        String s = IpToAddrUtil.sendGet("61.159.126.129", "JO5BZ-WYS3K-F6CJI-AAFAR-W327Q-3LFLY");
//        String cityInfo = IpToAddrUtil.getCityInfo("61.159.126.129");
        System.out.println(s);
    }

}
