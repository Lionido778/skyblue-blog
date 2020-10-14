package cn.codeprobe.blog.controller.admin;

import cn.codeprobe.blog.intercept.CheckRepeatCommit;
import cn.codeprobe.blog.pojo.Article;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/article")
public class ArticleAdminApi {

    @Autowired
    private ArticleService articleService;

    @PreAuthorize("@permission.admin()")
    @CheckRepeatCommit
    @PostMapping
    public ResponseResult addArticle(@RequestBody Article article) {
        return articleService.addArticle(article);
    }

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{articleId}")
    public ResponseResult deleteArticle(@PathVariable("articleId") String articleId) {
        return articleService.deleteArticle(articleId);
    }


    @GetMapping("/{articleId}")
    public ResponseResult getArticle(@PathVariable("articleId") String articleId) {
        return articleService.getArticle(articleId);
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/{articleId}")
    public ResponseResult updateArticle(@PathVariable("articleId") String articleId, @RequestBody Article article) {
        return articleService.updateArticle(articleId,article);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listArticles(@PathVariable("page") int page, @PathVariable("size") int size,
                                       @RequestParam(value = "categoryId", required = false) String categoryId,
                                       @RequestParam(value = "keyWord", required = false) String keyWord,
                                       @RequestParam(value = "state", required = false) String state) {
        return articleService.listArticles(page, size, categoryId, keyWord, state);
    }

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/status/{articleId}")
    public ResponseResult deleteArticleByState(@PathVariable("articleId") String articleId) {
        return articleService.deleteArticleByUpdateState(articleId);
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/top/{articleId}")
    public ResponseResult topArticle(@PathVariable("articleId") String articleId) {
        return articleService.topArticle(articleId);
    }
}