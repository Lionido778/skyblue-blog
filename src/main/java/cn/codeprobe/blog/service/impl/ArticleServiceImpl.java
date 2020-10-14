package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.ArticleDao;
import cn.codeprobe.blog.dao.ArticleNoContentDao;
import cn.codeprobe.blog.dao.CommentDao;
import cn.codeprobe.blog.dao.LabelDao;
import cn.codeprobe.blog.pojo.*;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.ArticleService;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.SolrService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ArticleServiceImpl extends BaseService implements ArticleService {

    @Autowired
    private UserService userService;
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private ArticleNoContentDao articleNoContentDao;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    private LabelDao labelDao;
    @Autowired
    private SolrService solrService;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * TODO:定时发布功能
     * Article: 1.发布文章  2.保存为草稿
     *
     * @param article
     * @return
     */
    @Override
    public ResponseResult addArticle(Article article) {
        //检查是发布文章，还是保存草稿
        String state = article.getState();
        if (!Constants.Article.STATE_DRAFT.equals(state) && !Constants.Article.STATE_PUBLISH.equals(state)) {
            return ResponseResult.FAILED("不支持此类型操作");
        }
        //检查登陆状态，获取当前用户
        User currentUser = userService.checkLoginStatus();
        if (currentUser == null) {
            return ResponseResult.NOT_LOGIN();
        }
        //Content类型 （"0" ==> 富文本 ，"1" ==> markdown）
        if (!"0".equals(article.getType()) && !"1".equals(article.getType())) {
            return ResponseResult.FAILED("文章类型有误");
        }
        //发布文章：需要检查数据  (若保存为草稿则不执行检查操作)
        if (Constants.Article.STATE_PUBLISH.equals(state)) {
            //检查数据(title、categoryId、content、type、summary、label)
            log.info("article_title ==> " + article.getTitle());
            if (article.getTitle() == null) {
                return ResponseResult.FAILED("标题不可以为空");
            }
            //标题长度
            if (article.getTitle().length() > Constants.Article.TITLE_MAX_LENGTH) {
                return ResponseResult.FAILED("文章标题长度超过最大限制" + Constants.Article.TITLE_MAX_LENGTH + "字符");
            }
            if (article.getCategoryId() == null) {
                return ResponseResult.FAILED("分类不可以为空");
            }
            if (article.getContent() == null) {
                return ResponseResult.FAILED("内容不可以为空");
            }
            if (article.getType() == null) {
                return ResponseResult.FAILED("文章类型不可以为空");
            }
            if (article.getSummary() == null) {
                return ResponseResult.FAILED("文章概要不可以为空");
            }
            if (article.getSummary().length() > Constants.Article.SUMMARY_MAX_LENGTH) {
                return ResponseResult.FAILED("文章概要长度超过最大限制" + Constants.Article.SUMMARY_MAX_LENGTH + "字符");
            }
            if (article.getLabel() == null) {
                return ResponseResult.FAILED("标签不可以为空");
            }
        }
        //通过articleId是否存在，来判断是更新文章/草稿，还是保存新草稿/发布新文章（但是已提交的文章，不可以再保存为草稿）
        if (article.getId() == null) {
            //保存新草稿/发布新文章
            article.setId(snowflakeIdWorker.nextId() + "");
            article.setUserId(currentUser.getId());
            article.setViewCount(0L);
            article.setCreateTime(new Date());
        } else {
            //更新文章/草稿
            //判断是否是已提交过的文章
            Article dbArticle = articleDao.findOneById(article.getId());
            if (Constants.Article.STATE_PUBLISH.equals(dbArticle.getState())
                    && Constants.Article.STATE_DRAFT.equals(state)) {
                return ResponseResult.FAILED("已提交的文章不可以保存为草稿");
            }
        }
        article.setUpdateTime(new Date());
        //保存到mysql
        articleDao.save(article);
        //如果是发表文章
        if (Constants.Article.STATE_PUBLISH.equals(state)) {
            //保存到solr数据库
            solrService.addArticle(article);
            //保存标签
            setupLabel(article);
            //删除redis里的文章列表
            redisUtil.del(Constants.Article.CACHE_FIRST_PAGE_ARTICLE);
        }
        //返回结果
        return ResponseResult.SUCCESS(Constants.Article.STATE_PUBLISH.equals(state) ? "文章发表成功" : "草稿保存成功");
    }

    //保存标签
    private void setupLabel(Article article) {
        String label = article.getLabel();
        List<String> labelList = new ArrayList<>();
        if (label.contains("-")) {
            List<String> labels = Arrays.asList(label.split("-"));
            labelList.addAll(labels);
        } else {
            labelList.add(label);
        }
        for (String item : labelList) {
            Label dbLabel = labelDao.findOneByName(item);
            if (dbLabel == null) {
                dbLabel = new Label();
                dbLabel.setId(snowflakeIdWorker.nextId() + "");
                dbLabel.setCount(0);
                dbLabel.setName(item);
                dbLabel.setCreateTime(new Date());
            }
            long count = dbLabel.getCount();
            dbLabel.setUpdateTime(new Date());
            dbLabel.setCount(++count);
            labelDao.save(dbLabel);
        }
    }

    /**
     * 这里因为文章内容太多，为了加快传输速度，所以使用 ArticleNoContent 类（没有content属性）来映射数据库的记录（不带content）
     *
     * @param page
     * @param size
     * @param categoryId
     * @param keyWord
     * @param state
     * @return
     */
    @Override
    public ResponseResult listArticles(int page, int size, String categoryId, String keyWord, String state) {
        //检查数据
        page = checkPage(page);
        size = checkSize(size);
        if (page == 1) {
            String cacheJson = (String) redisUtil.get(Constants.Article.CACHE_FIRST_PAGE_ARTICLE);
            if (!StringUtil.isEmpty(cacheJson)) {
                log.info("从缓存读取第一页文章列表");
                PageList<ArticleNoContent> result = gson.fromJson(cacheJson, new TypeToken<PageList<ArticleNoContent>>() {
                }.getType());
                return ResponseResult.SUCCESS("获取列表成功").setData(result);
            }
        }
        //创建条件
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        //条件查询
        Page<ArticleNoContent> all = articleNoContentDao.findAll(new Specification<ArticleNoContent>() {

            List<Predicate> predicates = new ArrayList<>();

            @Override
            public Predicate toPredicate(Root<ArticleNoContent> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                if (!StringUtil.isEmpty(categoryId)) {
                    Predicate categoryIdPre = cb.equal(root.get("categoryId").as(String.class), categoryId);
                    predicates.add(categoryIdPre);
                }
                if (!StringUtil.isEmpty(state)) {
                    Predicate statePre = cb.equal(root.get("state").as(String.class), state);
                    predicates.add(statePre);
                }
                if (!StringUtil.isEmpty(keyWord)) {
                    Predicate titleKeyWordPre = cb.like(root.get("title").as(String.class), "%" + keyWord + "%");
                    predicates.add(titleKeyWordPre);
                }
                Predicate[] predicateArray = new Predicate[predicates.size()];
                predicates.toArray(predicateArray);
                return cb.and(predicateArray);
            }
        }, pageable);
        //将第一页评论保存到redis缓存
        PageList<ArticleNoContent> firstPage = new PageList<>();
        if (page == 1) {
            firstPage.parsePage(all);
            log.info("将第一页文章列表加入缓存");
            redisUtil.set(Constants.Article.CACHE_FIRST_PAGE_ARTICLE, gson.toJson(firstPage), 5 * Constants.TimeValueInSeconds.MIN);
        }
        return ResponseResult.SUCCESS("文章列表查询成功").setData(firstPage);
    }

    @Autowired
    private Gson gson;

    /**
     * 通过articleId获取文章
     * 缓存机制
     *
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult getArticle(String articleId) {
        //redis
        String JsonArticle = (String) redisUtil.get(Constants.Article.CACHE_ARTICLE + articleId);
        if (!StringUtil.isEmpty(JsonArticle)) {
            log.info("从缓存中去拿文章");
            Article article = gson.fromJson(JsonArticle, Article.class);
            redisUtil.incr(Constants.Article.CACHE_VIEW_COUNT + articleId, 1);
            return ResponseResult.SUCCESS("获取成功").setData(article);
        }
        //redis里没有，去mysql中取
        Article dbArticle = articleDao.findOneById(articleId);
        if (dbArticle == null) {
            return ResponseResult.FAILED("该文章不存在");
        }
        //检查登陆状态
        User currentUser = userService.checkLoginStatus();
        //未登录用户/普通用户: 只能看到已发布的文章，和置顶文章
        if (currentUser == null || Constants.User.ROLE_NORMAL.equals(currentUser.getRoles())) {
            String state = dbArticle.getState();
            if (Constants.Article.STATE_PUBLISH.equals(state) || Constants.Article.STATE_TOP.equals(state)) {
                return setupArticleCache(articleId, dbArticle);
            }
        } else {
            //管理员
            return setupArticleCache(articleId, dbArticle);
        }
        return ResponseResult.PERMISSION_DENIED();
    }


    private ResponseResult setupArticleCache(String articleId, Article dbArticle) {
        //当获取文章时将已发表，或置顶的文章放入缓存
        String viewCount = (String) redisUtil.get(Constants.Article.CACHE_VIEW_COUNT + articleId);
        if (StringUtil.isEmpty(viewCount)) {
            log.info("文章加入缓存中");
            long newViewCount = dbArticle.getViewCount() + 1;
            redisUtil.set(Constants.Article.CACHE_VIEW_COUNT + articleId, String.valueOf(newViewCount));
        } else {
            long newViewCount = redisUtil.incr(Constants.Article.CACHE_VIEW_COUNT + articleId, 1);
            dbArticle.setViewCount(newViewCount);
            articleDao.save(dbArticle);
            solrService.updateArticle(dbArticle);
        }
        redisUtil.set(Constants.Article.CACHE_ARTICLE + articleId, gson.toJson(dbArticle), 5 * Constants.TimeValueInSeconds.MIN);
        return ResponseResult.SUCCESS("获取成功").setData(dbArticle);
    }

    /**
     * 更新文章
     * 可更新的内容：title,summary,cover,content,category,label,updateTime
     *
     * @param articleId
     * @param article
     * @return
     */
    @Override
    public ResponseResult updateArticle(String articleId, Article article) {
        //检查数据
        Article dbArticle = articleDao.findOneById(articleId);
        if (dbArticle == null) {
            return ResponseResult.FAILED("文章不存在");
        }
        //标题
        String newTitle = article.getTitle();
        if (!StringUtil.isEmpty(newTitle)) {
            dbArticle.setTitle(newTitle);
        }
        //概要
        String newSummary = article.getSummary();
        if (!StringUtil.isEmpty(newSummary)) {
            dbArticle.setSummary(newSummary);
        }
        //内容
        String newContent = article.getContent();
        if (!StringUtil.isEmpty(newContent)) {
            dbArticle.setContent(newContent);
        }
        //分类
        String newCategory = article.getCategoryId();
        if (!StringUtil.isEmpty(newCategory)) {
            dbArticle.setCategoryId(newCategory);
        }
        //标签
        String newLabel = article.getLabel();
        if (!StringUtil.isEmpty(newLabel)) {
            dbArticle.setLabel(newLabel);
        }
        //封面
        String newCover = article.getCover();
        if (!StringUtil.isEmpty(newCover)) {
            dbArticle.setCover(newCover);
        }
        dbArticle.setUpdateTime(new Date());
        articleDao.save(dbArticle);
        //删除redis中的数据
        redisUtil.del(Constants.Article.CACHE_ARTICLE + articleId);
        solrService.updateArticle(dbArticle);
        return ResponseResult.SUCCESS("更新成功");
    }

    @Autowired
    private CommentDao commentDao;

    /**
     * 删除文章: 物理删除
     *
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult deleteArticle(String articleId) {
        //先删评论
        commentDao.deleteAllByArticleId(articleId);
        //删除mysql中的文章
        int result = articleDao.deleteOneById(articleId);
        if (result > 0) {
            //删除solr中的文章
            solrService.deleteArticle(articleId);
            //删除redis中的文章详情缓存数据
            redisUtil.del(Constants.Article.CACHE_ARTICLE + articleId);
            //删除redis里的文章列表缓存数据
            redisUtil.del(Constants.Article.CACHE_FIRST_PAGE_ARTICLE);
            return ResponseResult.SUCCESS("删除成功");
        }
        return ResponseResult.FAILED("删除失败");
    }

    /**
     * 删除文章: 逻辑删除
     *
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult deleteArticleByUpdateState(String articleId) {
        //删除mysql中的文章
        int result = articleDao.deleteArticleByUpdateState(articleId);
        if (result > 0) {
            //删除solr中的文章
            solrService.deleteArticle(articleId);
            //删除redis中的文章详情缓存数据
            redisUtil.del(Constants.Article.CACHE_ARTICLE + articleId);
            //删除redis里的文章列表缓存数据
            redisUtil.del(Constants.Article.CACHE_FIRST_PAGE_ARTICLE);
            return ResponseResult.SUCCESS("删除成功");
        }
        return ResponseResult.FAILED("删除失败");
    }

    /**
     * 文章置顶
     *
     * @param articleId
     * @return
     */
    @Override
    public ResponseResult topArticle(String articleId) {
        Article dbArticle = articleDao.findOneById(articleId);
        if (dbArticle == null) {
            return ResponseResult.FAILED("文章不存在");
        }
        String state = dbArticle.getState();
        if (Constants.Article.STATE_PUBLISH.equals(state)) {
            dbArticle.setState(Constants.Article.STATE_TOP);
            return ResponseResult.SUCCESS("置顶成功");
        }
        if (Constants.Article.STATE_TOP.equals(state)) {
            dbArticle.setState(Constants.Article.STATE_PUBLISH);
            return ResponseResult.SUCCESS("取消置顶成功");
        }
        return ResponseResult.FAILED("不可以执行此操作");
    }

    @Override
    public ResponseResult getTopArticle() {
        List<ArticleNoContent> allTopArticles = articleNoContentDao.findAllByState(Constants.Article.STATE_TOP);
        return ResponseResult.SUCCESS("获取置顶文章成功").setData(allTopArticles);
    }

    /**
     * 获取推荐的文章
     * <p>
     * 算法：
     * 通过标签，获取一定数量的推荐文章，但要保证随机，每次推荐内容尽可能不雷同。
     * 如若标签推荐数量不够，此时补充最新文章来达到规定数量的文章。
     * 推荐文章里不可以有当前文章，同时值可以推荐；已发布的文章以及置顶文章
     *
     * @param articleId
     * @param size
     * @return
     */
    @Override
    public ResponseResult getRecommendArticles(String articleId, int size) {
        //获取文章标签
        String label = articleNoContentDao.findLabel(articleId);
        log.info("label ==> " + label);
        if (StringUtil.isEmpty(label)) {
            return ResponseResult.FAILED("获取失败");
        }
        //打散标签 ==> 标签库
        List<String> labelList = new ArrayList<>();
        if (!label.contains("-")) {
            labelList.add(label);
        } else {
            List<String> labels = Arrays.asList(label.split("-"));
            labelList.addAll(labels);
        }
        //从标签库随机获取一个标签,作为推荐搜索的关键字
        Random random = new Random();
        int i = random.nextInt(labelList.size());
        String targetLabel = labelList.get(i);
        log.info("targetLabel ==> " + targetLabel);
        //关键字模糊查询
        List<ArticleNoContent> all = articleNoContentDao.findAllByLikeLabel("%" + targetLabel + "%", articleId, size);
        if (all.size() < size) {
            List<ArticleNoContent> supplement = articleNoContentDao.findAllLatest("%" + targetLabel + "%", articleId, size - all.size());
            all.addAll(supplement);
        }
        return ResponseResult.SUCCESS("获取成功").setData(all);
    }

    /**
     * 获取标签云
     *
     * @param size
     * @return
     */
    @Override
    public ResponseResult getLabels(int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "count","createTime");
        Pageable pageable = PageRequest.of(0, size, sort);
        Page<Label> all = labelDao.findAll(pageable);
        return ResponseResult.SUCCESS("获取成功").setData(all);
    }

    /**
     * 通过 标签 获取对应文章列表
     *
     * @param label
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listArticlesByLabel(String label, int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<ArticleNoContent> all = articleNoContentDao.findAll(new Specification<ArticleNoContent>() {
            @Override
            public Predicate toPredicate(Root<ArticleNoContent> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate state1Pre = cb.equal(root.get("state").as(String.class), Constants.Article.STATE_PUBLISH);
                Predicate state2Pre = cb.equal(root.get("state").as(String.class), Constants.Article.STATE_TOP);
                Predicate or = cb.or(state1Pre, state2Pre);
                Predicate labelPre = cb.like(root.get("label").as(String.class), "%" + label + "%");
                return cb.and(or, labelPre);
            }
        }, pageable);
        return ResponseResult.SUCCESS("列表获取成功").setData(all);
    }

    /**
     * 通过 分类 获取对应文章列表
     *
     * @param category
     * @param page
     * @param size
     * @return
     */
    @Override
    public ResponseResult listArticlesByCategory(String category, int page, int size) {
        page = checkPage(page);
        size = checkSize(size);
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<ArticleNoContent> all = articleNoContentDao.findAll(new Specification<ArticleNoContent>() {
            @Override
            public Predicate toPredicate(Root<ArticleNoContent> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate state1Pre = cb.equal(root.get("state").as(String.class), Constants.Article.STATE_PUBLISH);
                Predicate state2Pre = cb.equal(root.get("state").as(String.class), Constants.Article.STATE_TOP);
                Predicate or = cb.or(state1Pre, state2Pre);
                Predicate labelPre = cb.equal(root.get("categoryId").as(String.class), category);
                return cb.and(or, labelPre);
            }
        }, pageable);
        return ResponseResult.SUCCESS("列表获取成功").setData(all);
    }
}
