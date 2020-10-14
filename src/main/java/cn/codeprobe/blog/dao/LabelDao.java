package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LabelDao extends JpaRepository<Label, String>, JpaSpecificationExecutor<Label> {
    Label findOneByName(String item);
}
