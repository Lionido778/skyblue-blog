package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserDao extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    User findOneById(String userId);

    User findOneByUsername(String username);

    User findOneByEmail(String email);

    User findOneByPhone(String phone);

    @Modifying
    @Query(nativeQuery = true, value = "update tb_user set state = 0 where id = ?")
    int deleteByState(String userId);

    @Modifying
    @Query(nativeQuery = true, value = "update tb_user set password = ? where email = ?")
    int updatePassword(String password, String email);

    @Modifying
    @Query(nativeQuery = true,value = "update tb_user set email = ? where id = ?")
    int updateEmail(String emailAddr, String id);
}
