package cn.codeprobe.blog.controller.admin;

import cn.codeprobe.blog.intercept.CheckRepeatCommit;
import cn.codeprobe.blog.pojo.Category;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/category")
public class CategoryApi {

    @Autowired
    private CategoryService categoryService;

    /**
     * 添加分类
     * 权限：管理员权限
     * <p>
     * 提交数据：
     * 1. 分类名称
     * 2. 分类拼音
     * 3. 分类描述
     * </p>
     * url : localhost:8080/admin/category
     *
     * @param category
     * @return
     */
    @PreAuthorize("@permission.admin()")  //通过注解的方式来控制权限
    @CheckRepeatCommit
    @PostMapping
    public ResponseResult addCategory(@RequestBody Category category) {
        return categoryService.addCategory(category);
    }

    /**
     * 删除分类
     *
     * @param categoryId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{categoryId}")
    public ResponseResult deleteCategory(@PathVariable("categoryId") String categoryId) {
        return categoryService.deleteCategoryByStatus(categoryId);
    }

    /**
     * 修改分类
     * 修改数据可有：分类名称，分类描述，分类拼音
     * 权限：管理员权限
     *
     * @param categoryId
     * @param category
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{categoryId}")
    public ResponseResult updateCategory(@PathVariable("categoryId") String categoryId, @RequestBody Category category) {
        return categoryService.updateCategory(categoryId,category);
    }

    /**
     * 通过ID获取分类
     *
     * <p>
     * 使用场景：在修改分类的时候，获取该分类，填充弹窗
     * 权限：管理员权限
     *
     * @param categoryId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{categoryId}")
    public ResponseResult getCategory(@PathVariable("categoryId") String categoryId) {
        return categoryService.getCategory(categoryId);
    }

    /**
     * 列出所有分类
     *
     * 权限：管理员权限
     *
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listCategories(@PathVariable("page") String page, @PathVariable("size") String size) {
        return categoryService.listCategories(page, size);
    }

}
