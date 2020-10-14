package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.ArticleNoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleNoContentDao extends JpaRepository<ArticleNoContent, String>, JpaSpecificationExecutor<ArticleNoContent> {

    ArticleNoContent findOneById(String id);

    @Query(nativeQuery = true, value = "SELECT * FROM `tb_article` WHERE `state` = ?")
    List<ArticleNoContent> findAllByState(String state);

    @Query(nativeQuery = true, value = "SELECT `labels` FROM `tb_article` WHERE id = ?")
    String findLabel(String articleId);

    @Query(nativeQuery = true, value = "SELECT * FROM `tb_article` WHERE `labels` LIKE ? AND `id` != ?  AND `state` in (1,3) LIMIT ?")
    List<ArticleNoContent> findAllByLikeLabel(String label, String articleId, int size);

    @Query(nativeQuery = true, value = "SELECT * FROM `tb_article` WHERE `labels` NOT LIKE ? AND `id` != ? AND `state` in (1,3) ORDER BY `create_time` DESC LIMIT ?")
    List<ArticleNoContent> findAllLatest(String targetLabel, String articleId, int supplement);

}
