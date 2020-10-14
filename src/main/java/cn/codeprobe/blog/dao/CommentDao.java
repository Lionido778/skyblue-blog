package cn.codeprobe.blog.dao;

import cn.codeprobe.blog.pojo.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommentDao extends JpaRepository<Comment,String>, JpaSpecificationExecutor<Comment> {

    Comment findOneById(String commentId);

    void deleteAllByArticleId(String articleId);

}
