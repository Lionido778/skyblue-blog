package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.Looper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface LooperDao extends JpaRepository<Looper, String>, JpaSpecificationExecutor<Looper> {
    Looper findOneById(String looperId);

    @Modifying
    @Query(nativeQuery = true,value = "UPDATE `tb_looper` SET `state` = '0' WHERE `id` = ?")
    int deleteOneByUpdateState(String looperId);
}
