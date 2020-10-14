package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.FriendLinkDao;
import cn.codeprobe.blog.pojo.FriendLink;
import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.FriendLinkService;
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
public class FriendLinkServiceImpl extends BaseService implements FriendLinkService {

    @Autowired
    private FriendLinkDao friendLinkDao;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    private UserService userService;

    /**
     * 添加友情链接
     *
     * @param friendLink
     * @return
     */
    @Override
    public ResponseResult addFriendLink(FriendLink friendLink) {
        //检查数据
        if (StringUtil.isEmpty(friendLink.getName())) {
            return ResponseResult.FAILED("链接名称不可以为空");
        }
        if (StringUtil.isEmpty(friendLink.getLogo())) {
            return ResponseResult.FAILED("链接Logo不可以为空");
        }
        if (StringUtil.isEmpty(friendLink.getUrl())) {
            return ResponseResult.FAILED("链接url不可以为空");
        }
        //补充数据
        friendLink.setId(snowflakeIdWorker.nextId() + "");
        friendLink.setOrder(2);
        friendLink.setState("1");
        friendLink.setCreateTime(new Date());
        friendLink.setUpdateTime(new Date());
        //保存
        friendLinkDao.save(friendLink);
        //返回结果
        return ResponseResult.SUCCESS("添加成功");
    }

    /**
     * 获取友情链接
     *
     * @param friendLinkId
     * @return
     */
    @Override
    public ResponseResult getFriendLink(String friendLinkId) {
        //查询
        FriendLink dbFriendLink = friendLinkDao.findOneById(friendLinkId);
        //返回查询结果
        return dbFriendLink == null ? ResponseResult.FAILED("获取失败，该友情链接不存在") : ResponseResult.SUCCESS("获取成功").setData(dbFriendLink);
    }

    /**
     * 获取友情链接列表(分页查询)
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listFriendLinks(String page, String size) {
        //检查登陆状态
        User currentUser = userService.checkLoginStatus();
        //检查数据
        int pageNum = checkPage(page);
        int sizeNum = checkSize(size);
        //创建条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime", "order");
        Pageable pageable = PageRequest.of(pageNum - 1, sizeNum, sort);
        //普通用户/未登录用户
        if (currentUser == null || Constants.User.ROLE_NORMAL.equals(currentUser.getRoles())) {
            Page<FriendLink> all = friendLinkDao.findAll(new Specification<FriendLink>() {
                @Override
                public Predicate toPredicate(Root<FriendLink> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                    //条件查询 state = 1
                    return cb.equal(root.get("state").as(String.class), "1");
                }
            }, pageable);
            return ResponseResult.SUCCESS("列表获取成功").setData(all);
        }
        //管理员
        Page<FriendLink> all = friendLinkDao.findAll(pageable);
        return ResponseResult.SUCCESS("列表获取成功").setData(all);
    }

    /**
     * 更新友情链接
     * 可更新数据：name、url、logo、order
     *
     * @param friendLinkId
     * @param friendLink
     * @return
     */
    @Override
    public ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink) {
        //查询
        FriendLink dbFriendLink = friendLinkDao.findOneById(friendLinkId);
        if (dbFriendLink == null) {
            return ResponseResult.FAILED("更新失败，该友情链接不存在");
        }
        //更新内容
        if (!StringUtil.isEmpty(friendLink.getName())) {
            dbFriendLink.setName(friendLink.getName());
        }
        if (!StringUtil.isEmpty(friendLink.getUrl())) {
            dbFriendLink.setUrl(friendLink.getUrl());
        }
        if (!StringUtil.isEmpty(friendLink.getLogo())) {
            dbFriendLink.setLogo(friendLink.getLogo());
        }
        dbFriendLink.setOrder(friendLink.getOrder());
        dbFriendLink.setUpdateTime(new Date());
        //保存
        friendLinkDao.save(dbFriendLink);
        return ResponseResult.SUCCESS("更新成功");
    }

    /**
     * 删除友情列表
     *
     * @param friendLinkId
     * @return
     */
    @Override
    public ResponseResult deleteByUpdateState(String friendLinkId) {
        //查询要删除的友情链接
        FriendLink dbFriendLink = friendLinkDao.findOneById(friendLinkId);
        if (dbFriendLink == null) {
            return ResponseResult.FAILED("删除失败，该友情链接不存在");
        }
        //删除
        int result = friendLinkDao.deleteAllById(friendLinkId);
        return result > 0 ? ResponseResult.SUCCESS("删除成功") : ResponseResult.FAILED("删除失败");
    }
}
