package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.LooperDao;
import cn.codeprobe.blog.pojo.Looper;
import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.LooperService;
import cn.codeprobe.blog.service.UserService;
import cn.codeprobe.blog.constatnts.Constants;
import cn.codeprobe.blog.utils.id.SnowflakeIdWorker;
import cn.codeprobe.blog.utils.common.StringUtil;
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

@Transactional
@Service
public class LooperServiceImpl extends BaseService implements LooperService {

    @Autowired
    private LooperDao looperDao;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    private UserService userService;

    @Override
    public ResponseResult addLoop(Looper looper) {
        //检查数据
        if (StringUtil.isEmpty(looper.getTitle())) {
            return ResponseResult.FAILED("标题不可以为空");
        }
        if (StringUtil.isEmpty(looper.getImageUrl())) {
            return ResponseResult.FAILED("图片url不可以为空");
        }
        if (StringUtil.isEmpty(looper.getTargetUrl())) {
            return ResponseResult.FAILED("跳转链接不可以为空");
        }
        //补充数据
        looper.setId(snowflakeIdWorker.nextId() + "");
        looper.setState("1");
        looper.setCreateTime(new Date());
        looper.setUpdateTime(new Date());
        //保存
        looperDao.save(looper);
        //返回结果
        return ResponseResult.SUCCESS("轮播图添加成功");
    }

    @Override
    public ResponseResult deleteLoop(String looperId) {
        int result = looperDao.deleteOneByUpdateState(looperId);
        return result > 0 ? ResponseResult.SUCCESS("轮播图删除成功") : ResponseResult.FAILED("轮播图删除失败");
    }

    @Override
    public ResponseResult getLoop(String looperId) {
        Looper dbLooper = looperDao.findOneById(looperId);
        if (dbLooper == null) {
            return ResponseResult.FAILED("该轮播图不存在");
        }
        return ResponseResult.SUCCESS("轮播图获取成功").setData(dbLooper);
    }

    @Override
    public ResponseResult updateLoop(String looperId, Looper looper) {
        Looper dbLooper = looperDao.findOneById(looperId);
        if (dbLooper == null) {
            return ResponseResult.FAILED("该轮播图不存在");
        }
        if (!StringUtil.isEmpty(looper.getTitle())) {
            dbLooper.setTitle(looper.getTitle());
        }
        if (!StringUtil.isEmpty(looper.getImageUrl())) {
            dbLooper.setImageUrl(looper.getImageUrl());
        }
        if (!StringUtil.isEmpty(looper.getTargetUrl())) {
            dbLooper.setTargetUrl(looper.getTargetUrl());
        }
        if (!StringUtil.isEmpty(looper.getState())) {
            dbLooper.setState(looper.getState());
        }
        dbLooper.setOrder(looper.getOrder());
        dbLooper.setUpdateTime(new Date());
        looperDao.save(dbLooper);
        return ResponseResult.SUCCESS("更新成功");
    }

    @Override
    public ResponseResult listLoops(String page, String size) {
        //检查登陆状态
        User currentUser = userService.checkLoginStatus();
        int pageNum = checkPage(page);
        int sizeNum = checkSize(size);
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime", "order");
        Pageable pageable = PageRequest.of(pageNum - 1, sizeNum, sort);
        //普通用户/未登录用户
        if (currentUser == null || Constants.User.ROLE_NORMAL.equals(currentUser.getRoles())) {
            Page<Looper> all = looperDao.findAll(new Specification<Looper>() {
                @Override
                public Predicate toPredicate(Root<Looper> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                    return cb.equal(root.get("state").as(String.class), "1");
                }
            }, pageable);
            return ResponseResult.SUCCESS("轮播图列表获取成功").setData(all);
        }
        //管理员
        Page<Looper> all = looperDao.findAll(pageable);
        return ResponseResult.SUCCESS("轮播图列表获取成功").setData(all);
    }
}
