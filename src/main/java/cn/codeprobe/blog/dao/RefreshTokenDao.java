package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenDao extends JpaRepository<RefreshToken, String>, JpaSpecificationExecutor<RefreshToken> {

    RefreshToken findOneByTokenKey(String tokenKey);

    RefreshToken findOneByMobileTokenKey(String tokenKey);

    @Query(nativeQuery = true, value = "SELECT `token_key` FROM `tb_refresh_token` WHERE `user_id` = ?")
    String findTokenKeyById(String userId);

    @Query(nativeQuery = true, value = "SELECT `mobile_token_key` FROM `tb_refresh_token` WHERE `user_id` = ?")
    String findMobileTokenKeyById(String userId);

    void deleteOneByTokenKey(String tokenKey);

    RefreshToken findOneById(String id);

    RefreshToken findOneByUserId(String id);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE `tb_refresh_token` SET `mobile_token_key` = '' WHERE `user_id` = ?")
    int deleteMobileTokenKey(String id);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE `tb_refresh_token` SET `token_key` = '' WHERE `user_id` = ?")
    int deleteTokenKey(String id);
}
