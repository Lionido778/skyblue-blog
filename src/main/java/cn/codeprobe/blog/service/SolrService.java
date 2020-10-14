package cn.codeprobe.blog.service;

import cn.codeprobe.blog.pojo.Article;
import cn.codeprobe.blog.response.ResponseResult;

public interface SolrService {

    void addArticle(Article article);

    void deleteArticle(String articleId);

    void updateArticle(Article article);

    ResponseResult doSearch(String keyWord, int page, int size, String CategoryId, Integer sort);
}
