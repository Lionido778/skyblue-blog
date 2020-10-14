package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SettingDao extends JpaRepository<Setting, String>, JpaSpecificationExecutor<Setting> {

    Setting findOneByKey (String key);
}
