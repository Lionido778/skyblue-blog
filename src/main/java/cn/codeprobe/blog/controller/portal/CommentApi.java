package cn.codeprobe.blog.controller.portal;

import cn.codeprobe.blog.intercept.CheckRepeatCommit;
import cn.codeprobe.blog.pojo.Comment;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/comment")
public class CommentApi {

    @Autowired
    private CommentService commentService;

    /**
     * 添加评论
     *
     * @param comment
     * @return
     */
    @CheckRepeatCommit
    @PostMapping
    public ResponseResult addComment(@RequestBody Comment comment) {
        return commentService.addComment(comment);
    }

    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    @DeleteMapping("/{commentId}")
    public ResponseResult deleteComment(@PathVariable("commentId") String commentId) {
        return commentService.deleteCommentByCommentID(commentId);
    }

    /**
     * 获取对应文章下的评论
     *
     * @param articleId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{articleId}/{page}/{size}")
    public ResponseResult listCommentsByArticleId(@PathVariable("articleId") String articleId,
                                                  @PathVariable("page") int page,
                                                  @PathVariable("size") int size) {
        return commentService.listCommentsByArticle(articleId, page, size);
    }
}
