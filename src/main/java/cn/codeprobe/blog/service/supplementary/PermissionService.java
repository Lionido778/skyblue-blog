package cn.codeprobe.blog.service.supplementary;

import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.service.UserService;
import cn.codeprobe.blog.constatnts.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("permission")
public class PermissionService {

    @Autowired
    private UserService userService;

    public boolean admin(){
        // 判断登陆状态
        User user = userService.checkLoginStatus();
        if (user == null) {
            return false;
        }
        // 判断是否具有管理员权限
        if (user.getRoles().equals(Constants.User.ROLE_ADMIN)) {
            return true;
        }
        return false;
    }

}
