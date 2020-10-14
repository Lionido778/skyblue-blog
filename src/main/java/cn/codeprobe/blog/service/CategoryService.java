package cn.codeprobe.blog.service;

import cn.codeprobe.blog.pojo.Category;
import cn.codeprobe.blog.response.ResponseResult;


public interface CategoryService {

    ResponseResult addCategory(Category category);

    ResponseResult getCategory(String categoryId);

    ResponseResult listCategories(String page, String size);

    ResponseResult updateCategory(String categoryId, Category category);

    ResponseResult deleteCategoryByStatus(String categoryId);
}
