package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.CategoryDao;
import cn.codeprobe.blog.pojo.Category;
import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.CategoryService;
import cn.codeprobe.blog.service.UserService;
import cn.codeprobe.blog.constatnts.Constants;
import cn.codeprobe.blog.utils.id.SnowflakeIdWorker;
import cn.codeprobe.blog.utils.common.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

@Slf4j
@Transactional
@Service
public class CategoryServiceImpl extends BaseService implements CategoryService {

    @Autowired
    private CategoryDao categoryDao;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    private UserService userService;

    /**
     * 添加分类
     *
     * @param category
     * @return
     */
    @Override
    public ResponseResult addCategory(Category category) {
        // 检查数据
        if (StringUtil.isEmpty(category.getName())) {
            return ResponseResult.FAILED("分类名称不可以为空");
        }
        if (StringUtil.isEmpty(category.getPinyin())) {
            return ResponseResult.FAILED("分类拼音不可以为空");
        }
        if (StringUtil.isEmpty(category.getDescription())) {
            return ResponseResult.FAILED("分类描述不可以为空");
        }
        // 补充数据
        category.setId(snowflakeIdWorker.nextId() + "");
        category.setOrder(2);
        category.setState("1");
        category.setCreateTime(new Date());
        category.setUpdateTime(new Date());
        // 保存分类
        categoryDao.save(category);
        // 返回结果
        return ResponseResult.SUCCESS("分类添加成功");
    }

    /**
     * 通过ID获取分类
     *
     * @param categoryId
     * @return
     */
    @Override
    public ResponseResult getCategory(String categoryId) {
        if (StringUtil.isEmpty(categoryId)) {
            return ResponseResult.FAILED("分类ID不可以为空");
        }
        Category dbCategory = categoryDao.findOneById(categoryId);
        if (dbCategory == null) {
            return ResponseResult.FAILED("分类不存在");
        }
        return ResponseResult.SUCCESS("分类查找成功").setData(dbCategory);
    }

    /**
     * 获取分类列表
     * 管理员：可以查看所有分类
     * 普通用户：只可以查看有效分类
     *
     * @param page  string 类型可以接受前端出传来的空值
     * @param size  
     * @return
     */
    @Override
    public ResponseResult listCategories(String page, String size) {
        //检查登录状态
        User currentUser = userService.checkLoginStatus();
        // 参数检查
        int pageNum = checkPage(page);
        int sizeNum = checkSize(size);
        // 创建条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime", "order");
        Pageable pageable = PageRequest.of(pageNum - 1, sizeNum, sort);
        //普通用户/未登录用户
        if (currentUser == null || Constants.User.ROLE_NORMAL.equals(currentUser.getRoles())) {
            // 条件查询 state = 1 (有效)
            Page<Category> all = categoryDao.findAll(new Specification<Category>() {
                @Override
                public Predicate toPredicate(Root<Category> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                    return cb.equal(root.get("state").as(String.class), "1");
                }
            }, pageable);
            // 返回结果
            return ResponseResult.SUCCESS("获取分类列表成功").setData(all);
        }
        //管理员
        Page<Category> all = categoryDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取分类列表成功").setData(all);
    }

    /**
     * 更新分类
     *
     * @param categoryId
     * @param category
     * @return
     */
    @Override
    public ResponseResult updateCategory(String categoryId, Category category) {
        Category dbCategory = categoryDao.findOneById(categoryId);
        // 检查数据
        if (dbCategory == null) {
            return ResponseResult.FAILED("该分类不存在");
        }
        // 修改内容
        if (!StringUtil.isEmpty(category.getName())) {
            dbCategory.setName(category.getName());
        }
        if (!StringUtil.isEmpty(category.getDescription())) {
            dbCategory.setDescription(category.getDescription());
        }
        if (!StringUtil.isEmpty(category.getPinyin())) {
            dbCategory.setPinyin(category.getPinyin());
        }
        dbCategory.setUpdateTime(new Date());
        // 保存
        categoryDao.save(dbCategory);
        // 返回结果
        return ResponseResult.SUCCESS("分类更新成功");
    }

    /**
     * 删除分类
     * <p>
     * 实现方式：通过修改分类状态："0" ==> 不可用、"1" ==> 可用
     * 因为category 由于外键约束，不可以真正删除
     *
     * @param categoryId
     * @return
     */
    @Override
    public ResponseResult deleteCategoryByStatus(String categoryId) {
        int result = categoryDao.deleteByModifyStatus(categoryId);
        return result > 0 ? ResponseResult.SUCCESS("删除分类成功") : ResponseResult.FAILED("删除分类失败");
    }
}
