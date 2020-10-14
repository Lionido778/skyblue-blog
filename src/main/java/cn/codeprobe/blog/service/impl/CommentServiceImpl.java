package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.ArticleNoContentDao;
import cn.codeprobe.blog.dao.CommentDao;
import cn.codeprobe.blog.dao.UserNoPasswordDao;
import cn.codeprobe.blog.pojo.ArticleNoContent;
import cn.codeprobe.blog.pojo.Comment;
import cn.codeprobe.blog.pojo.User;
import cn.codeprobe.blog.pojo.UserNoPassword;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.CommentService;
import cn.codeprobe.blog.service.supplementary.AsyncService;
import cn.codeprobe.blog.service.UserService;
import cn.codeprobe.blog.utils.common.PageList;
import cn.codeprobe.blog.utils.common.RedisUtil;
import cn.codeprobe.blog.utils.id.SnowflakeIdWorker;
import cn.codeprobe.blog.utils.common.StringUtil;
import cn.codeprobe.blog.constatnts.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Transient;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

@Slf4j
@Transient
@Service
public class CommentServiceImpl extends BaseService implements CommentService {

    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ArticleNoContentDao articleNoContentDao;
    @Autowired
    private UserService userService;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    private UserNoPasswordDao userNoPasswordDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private Gson gson;
    @Autowired
    private AsyncService asyncService; //异步发送邮件


    @Override
    public ResponseResult addComment(Comment comment) {
        //检查数据
        String articleId = comment.getArticleId();
        if (StringUtil.isEmpty(articleId)) {
            return ResponseResult.FAILED("文章ID不可以为空");
        }
        ArticleNoContent dbArticle = articleNoContentDao.findOneById(articleId);
        if (dbArticle == null) {
            return ResponseResult.FAILED("该文章不存在");
        }
        if (StringUtil.isEmpty(comment.getContent())) {
            return ResponseResult.FAILED("评论内容不可以为空");
        }
        //检查登陆状态
        User currentUser = userService.checkLoginStatus();
        if (currentUser == null) {
            return ResponseResult.NOT_LOGIN();
        }
        //补充数据
        comment.setId(snowflakeIdWorker.nextId() + "");
        comment.setUsername(currentUser.getUsername());
        comment.setUserId(currentUser.getId());
        comment.setUserAvatar(currentUser.getAvatar());
        comment.setState("1");
        comment.setCreateTime(new Date());
        comment.setUpdateTime(new Date());
        //发送邮件通知
        String authorId = dbArticle.getUserId();
        UserNoPassword author = userNoPasswordDao.findOneById(authorId);
        String emailAddr = author.getEmail();
        String authorUsername = author.getUsername();
        String title = dbArticle.getTitle();
        try {
            asyncService.sendCommentNotice(authorUsername, title, emailAddr);
            log.info("评论邮箱提醒发送成功");
        } catch (Exception e) {
            return ResponseResult.FAILED("评论提醒发送失败,请稍后重试");
        }
        //保存评论
        commentDao.save(comment);
        //删除缓存里的第一页评论
        redisUtil.del((Constants.Comment.CACHE_FIRST_PAGE_COMMENT + articleId));
        return ResponseResult.SUCCESS("评论成功");
    }

    /**
     * 获取一篇文章对应的评论
     * TODO：算法
     * 根据点赞量和创建时间，排序，新发表的评论再一段时间内，优先展示
     * <p>
     * 缓存机制：
     * 将每篇文章的第一页评论加入缓存，同时当有新的评论时，应删除缓存中的评论
     *
     * @param articleId
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listCommentsByArticle(String articleId, int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        //如果是第一页，从redis里获取
        if (page == 1) {
            String cacheJson = (String) redisUtil.get(Constants.Comment.CACHE_FIRST_PAGE_COMMENT + articleId);
            if (!StringUtil.isEmpty(cacheJson)) {
                log.info("从缓存读取第一页评论");
                PageList<Comment> result = gson.fromJson(cacheJson, new TypeToken<PageList<Comment>>() {
                }.getType());
                return ResponseResult.SUCCESS("获取列表成功").setData(result);
            }
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "state");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAll(new Specification<Comment>() {
            @Override
            public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                return cb.equal(root.get("articleId").as(String.class), articleId);
            }
        }, pageable);
        //将第一页评论保存到redis缓存
        PageList<Comment> firstPage = new PageList<>();
        firstPage.parsePage(all);
        if (page == 1) {
            log.info("将第一页评论加入缓存");
            redisUtil.set(Constants.Comment.CACHE_FIRST_PAGE_COMMENT + articleId, gson.toJson(firstPage), 5 * Constants.TimeValueInSeconds.MIN);
        }
        return ResponseResult.SUCCESS("获取列表成功").setData(firstPage);
    }


    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    @Override
    public ResponseResult deleteCommentByCommentID(String commentId) {
        //检查数据
        Comment dbComment = commentDao.findOneById(commentId);
        if (dbComment == null) {
            return ResponseResult.FAILED("该评论不存在");
        }
        //检查登陆状态
        User currentUser = userService.checkLoginStatus();
        if (currentUser == null) {
            return ResponseResult.NOT_LOGIN();
        }
        //删除权限: 只有管理员和评论的主人才可以删除评论
        if (dbComment.getUserId().equals(currentUser.getId()) || Constants.User.ROLE_ADMIN.equals(currentUser.getRoles())) {
            commentDao.deleteById(commentId);
            return ResponseResult.SUCCESS("评论删除成功");
        }
        return ResponseResult.PERMISSION_DENIED();
    }


    /**
     * 获取所有评论
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listComments(int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Comment> all = commentDao.findAll(pageable);
        return ResponseResult.SUCCESS("查询成功").setData(all);
    }

    @Override
    public ResponseResult topComment(String commentId) {
        Comment dbComment = commentDao.findOneById(commentId);
        if (dbComment == null) {
            return ResponseResult.FAILED("该评论不存在");
        }
        if (Constants.Comment.STATE_PUBLISH.equals(dbComment.getState())) {
            dbComment.setState(Constants.Comment.STATE_TOP);
            commentDao.save(dbComment);
            return ResponseResult.SUCCESS("置顶成功");
        } else if (Constants.Comment.STATE_TOP.equals(dbComment.getState())) {
            dbComment.setState(Constants.Comment.STATE_PUBLISH);
            commentDao.save(dbComment);
            return ResponseResult.SUCCESS("取消置顶");
        }
        return ResponseResult.FAILED("不能执行此操作");
    }
}
