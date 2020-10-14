package cn.codeprobe.blog.service;

import cn.codeprobe.blog.pojo.Article;
import cn.codeprobe.blog.response.ResponseResult;

public interface ArticleService {

    ResponseResult addArticle(Article article);

    ResponseResult listArticles(int page, int size, String categoryId, String keyWord, String state);

    ResponseResult getArticle(String articleId);

    ResponseResult updateArticle(String articleId, Article article);

    ResponseResult deleteArticle(String articleId);

    ResponseResult deleteArticleByUpdateState(String articleId);

    ResponseResult topArticle(String articleId);

    ResponseResult getTopArticle();

    ResponseResult getRecommendArticles(String articleId, int size);

    ResponseResult getLabels(int size);

    ResponseResult listArticlesByLabel(String label, int page, int size);

    ResponseResult listArticlesByCategory(String category, int page, int size);
}
