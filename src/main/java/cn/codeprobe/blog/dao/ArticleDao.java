package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArticleDao extends JpaRepository<Article, String>, JpaSpecificationExecutor<Article> {

    Article findOneById(String id);

    int deleteOneById(String articleId);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE `tb_article` SET `state` = '0' WHERE `id` = ?")
    int deleteArticleByUpdateState(String articleId);

}


