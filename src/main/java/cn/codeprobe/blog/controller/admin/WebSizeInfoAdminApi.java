package cn.codeprobe.blog.controller.admin;

import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.WebSizeInfoAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/web_size_info")
public class WebSizeInfoAdminApi {

    @Autowired
    private WebSizeInfoAdminService webSizeInfoAdminService;

    @PreAuthorize("@permission.admin()")
    @GetMapping("/title")
    public ResponseResult getTitle() {
        return webSizeInfoAdminService.getTitle();
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/title")
    public ResponseResult UpdateTitle(@RequestParam("title") String title) {
        return webSizeInfoAdminService.updateTitle(title);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/seo")
    public ResponseResult getSeoInfo() {
        return webSizeInfoAdminService.getSeoInfo();
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/seo")
    public ResponseResult updateSeoInfo(@RequestParam("keyword") String keyword, @RequestParam("description") String description) {
        return webSizeInfoAdminService.updateSeoInfo(keyword,description);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/view_count")
    public ResponseResult getWebViewCount() {
        return webSizeInfoAdminService.getViewCount();
    }

}
