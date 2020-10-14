package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.pojo.Article;
import cn.codeprobe.blog.pojo.SearchResult;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.supplementary.BaseService;
import cn.codeprobe.blog.service.SolrService;
import cn.codeprobe.blog.constatnts.Constants;
import cn.codeprobe.blog.utils.common.PageList;
import cn.codeprobe.blog.utils.common.StringUtil;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.SimTocExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.http.util.TextUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SolrServiceImpl extends BaseService implements SolrService {


    @Autowired
    private SolrClient solrClient;

    /**
     * 将文章添加入solr数据库中,同时该文章必须是要发布的文章
     *
     * @param article state = 1 (发布文章)
     */
    @Override
    public void addArticle(Article article) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", article.getId());
        doc.addField("blog_view_count", article.getViewCount());
        doc.addField("blog_title", article.getTitle());
        //格式转换: 文章的内容 可以是富文本（0）或者markdown格式（1）.但是solr数据库中我们只需要纯文本的内容
        String type = article.getType();
        //markdown ==> html
        String html = null;
        if (Constants.Article.TYPE_MARKDOWN.equals(type)) {
            MutableDataSet options = new MutableDataSet().set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    JekyllTagExtension.create(),
                    TocExtension.create(),
                    SimTocExtension.create()
            ));
            Parser parser = Parser.builder(options).build();
            HtmlRenderer renderer = HtmlRenderer.builder(options).build();
            Node document = parser.parse(article.getContent());
            html = renderer.render(document);
        } else if (Constants.Article.TYPE_RICH_TEXT.equals(type)) {
            html = article.getContent();
        }
        //html ==> text纯文本
        String content = Jsoup.parse(html).text();
        doc.addField("blog_content", content);
        doc.addField("blog_create_time", article.getCreateTime());
        doc.addField("blog_labels", article.getLabel());
        doc.addField("blog_url", "https://www.yzjblog.com");
        doc.addField("blog_category_id", article.getCategoryId());
        try {
            solrClient.add(doc);
            solrClient.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除solr数据库的内容
     * 当从mysql删除文章时，也需要从solr删除
     *
     * @param articleId articleId = id (solr)
     */
    @Override
    public void deleteArticle(String articleId) {
        try {
            solrClient.deleteById(articleId);
            solrClient.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新文章
     *
     * @param article
     */
    @Override
    public void updateArticle(Article article) {
        addArticle(article);
    }

    /**
     * doSearch 查询
     *
     * @param keyWord
     * @param page
     * @param size
     * @param categoryId
     * @param sort
     * @return
     */
    @Override
    public ResponseResult doSearch(String keyWord, int page, int size, String categoryId, Integer sort) {
        //检查page,size
        page = checkPage(page);
        size = checkSize(size);
        SolrQuery solrQuery = new SolrQuery();
        //分页设置
        //每页的数量
        solrQuery.set("rows", size);
        /*
         * 第一页： 0 ~ (size-1)
         * 第二页： size ~ ( 2 * size - 1)
         * 第三页： 2 * size ~ (3 * size - 1)
         * ...
         * */
        int start = (page - 1) * size;
        //开始的位置
        solrQuery.set("start", start);
        //关键字
        solrQuery.set("df", "search_item");
        //条件过滤
        if (StringUtil.isEmpty(keyWord)) {
            solrQuery.set("q", "*");
        } else {
            solrQuery.set("q", keyWord);
        }
        //排序
        if (sort != null) {
            if (sort == 1) {
                solrQuery.setSort("blog_create_time", SolrQuery.ORDER.asc);
            } else if (sort == 2) {
                solrQuery.setSort("blog_create_time", SolrQuery.ORDER.desc);
            } else if (sort == 3) {
                solrQuery.setSort("blog_view_count", SolrQuery.ORDER.asc);
            } else if (sort == 4) {
                solrQuery.setSort("blog_view_count", SolrQuery.ORDER.desc);
            }
        }
        //分类
        if (!TextUtils.isEmpty(categoryId)) {
            solrQuery.set("fq", "blog_category_id:" + categoryId);
        }
        //关键字高亮
        solrQuery.setHighlight(true);
        solrQuery.addHighlightField("blog_title,blog_content");
        solrQuery.setHighlightSimplePre("<font color='red'>");
        solrQuery.setHighlightSimplePost("</font>");
        solrQuery.setHighlightFragsize(500);
        //设置返回字段
        //blog_content,blog_create_time,blog_labels,blog_url,blog_title,blog_view_count
        solrQuery.addField("id,blog_content,blog_create_time,blog_labels,blog_url,blog_title,blog_view_count");
        //4、搜索
        try {
            //4.1、处理搜索结果
            QueryResponse result = solrClient.query(solrQuery);
            //获取到高亮内容
            Map<String, Map<String, List<String>>> highlighting = result.getHighlighting();
            //把数据转成bean类
             List<SearchResult> resultList = result.getBeans(SearchResult.class);
            //结果列表
            for (SearchResult item : resultList) {
                Map<String, List<String>> stringListMap = highlighting.get(item.getId());
                List<String> blogContent = stringListMap.get("blog_content");
                if (blogContent != null) {
                    item.setBlogContent(blogContent.get(0));
                }
                List<String> blogTitle = stringListMap.get("blog_title");
                if (blogTitle != null) {
                    item.setBlogTitle(blogTitle.get(0));
                }
            }
            //5、返回搜索结果
            //包含内容
            //列表、页面、每页数量
            long numFound = result.getResults().getNumFound();
            PageList<SearchResult> pageList = new PageList<>(page, size, numFound);
            pageList.setContents(resultList);
            //返回结果
            return ResponseResult.SUCCESS("搜索成功.").setData(pageList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.FAILED("搜索失败，请稍后重试.");
    }


}
