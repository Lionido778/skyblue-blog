package cn.codeprobe.blog;

import org.apache.solr.client.solrj.SolrClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BlogApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestSolr {

    @Autowired
    private SolrClient solrClient;

    @Test
    public void add() {
//        SolrInputDocument doc = new SolrInputDocument();
//        doc.addField("id", article.getId());
//        doc.addField("blog_view_count", article.getViewCount());
//        doc.addField("blog_title", article.getTitle());
//        doc.addField("blog_content", article.getContent());
//        doc.addField("blog_create_time", article.getCreateTime());
//        doc.addField("blog_labels", article.getLabel());
//        doc.addField("blog_url", "https://www.yzjblog.com");
//        doc.addField("blog_category_id", article.getCategoryId());
//        try {
//            solrClient.add(doc);
//            solrClient.commit();
//        } catch (SolrServerException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}
