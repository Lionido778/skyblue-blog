package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.UserNoPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserNoPasswordDao extends JpaRepository<UserNoPassword, String>, JpaSpecificationExecutor<UserNoPassword> {

    UserNoPassword findOneById(String authorId);

}
