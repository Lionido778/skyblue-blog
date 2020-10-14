package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ImageDao extends JpaRepository<Image,String>, JpaSpecificationExecutor<Image> {

    @Modifying
    @Query(nativeQuery = true,value = "UPDATE `tb_images` SET `state` = '0' WHERE `id` = ?")
    int deleteImageByUpdateState(String imageId);
}
