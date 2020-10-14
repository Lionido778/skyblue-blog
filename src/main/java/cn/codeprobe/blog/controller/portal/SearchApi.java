package cn.codeprobe.blog.controller.portal;

import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.SolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/search")
public class SearchApi {

    @Autowired
    private SolrService solrService;

    @GetMapping
    public ResponseResult doSearch(@RequestParam("keyword") String keyword,
                                   @RequestParam("page") int page,
                                   @RequestParam("size") int size,
                                   @RequestParam(value = "categoryId", required = false) String categoryId,
                                   @RequestParam(value = "sort", required = false) Integer sort) {
        return solrService.doSearch(keyword, page, size, categoryId, sort);
    }
}
