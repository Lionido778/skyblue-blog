package cn.codeprobe.blog.service;

import cn.codeprobe.blog.pojo.Comment;
import cn.codeprobe.blog.response.ResponseResult;

public interface CommentService {
    ResponseResult addComment(Comment comment);

    ResponseResult listCommentsByArticle(String articleId, int page, int size);

    ResponseResult deleteCommentByCommentID(String commentId);

    ResponseResult listComments(int page, int size);

    ResponseResult topComment(String commentId);
}
