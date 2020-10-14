package cn.codeprobe.blog.controller.portal;

import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.ArticleService;
import cn.codeprobe.blog.service.CategoryService;
import cn.codeprobe.blog.constatnts.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/article")
public class ArticleApi {

    @Autowired
    private ArticleService articleService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取文章列表
     *
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listArticles(@PathVariable("page") int page, @PathVariable() int size) {
        return articleService.listArticles(page, size, null, null, Constants.Article.STATE_PUBLISH);
    }

    /**
     * 通过 <分类> 获取对应分类下的文章
     *
     * @param category
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/category/{category}/{page}/{size}")
    public ResponseResult listArticlesByCategory(@PathVariable("category") String category,
                                                 @PathVariable("page") int page,
                                                 @PathVariable() int size) {

        return articleService.listArticlesByCategory(category, page, size);
    }

    /**
     * 通过 <标签> 获取对应标签下的文章
     *
     * @param label
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list/label/{label}/{page}/{size}")
    public ResponseResult listArticlesByLabel(@PathVariable("label") String label,
                                              @PathVariable("page") int page,
                                              @PathVariable() int size) {
        return articleService.listArticlesByLabel(label, page, size);
    }

    /**
     * 点击查看，获取一篇文章详细内容
     *
     * @param articleId
     * @return
     */
    @GetMapping("/detail/{articleId}")
    public ResponseResult getArticleDetails(@PathVariable("articleId") String articleId) {
        return articleService.getArticle(articleId);
    }

    /**
     * 点开一篇文章后，推荐类似文章
     *
     * @param articleId
     * @param size
     * @return
     */
    @GetMapping("/recommend/{articleId}/{size}")
    public ResponseResult getRecommendArticles(@PathVariable("articleId") String articleId,
                                               @PathVariable("size") int size) {
        return articleService.getRecommendArticles(articleId, size);
    }

    /**
     * 获取置顶文章
     *
     * @return
     */
    @GetMapping("/top")
    public ResponseResult getTopArticle() {
        return articleService.getTopArticle();
    }

    /**
     * 获取分类列表
     *
     * @return
     */
    @GetMapping("/categories")
    public ResponseResult getCategories(@RequestParam(value = "page", required = false) String page,
                                        @RequestParam(value = "size", required = false) String size) {
        return categoryService.listCategories(page, size);
    }

    /**
     * 获取标签云
     *
     * @param size
     * @return
     */
    @GetMapping("/labels/{size}")
    public ResponseResult getLabels(@PathVariable("size") int size) {
        return articleService.getLabels(size);
    }
}
