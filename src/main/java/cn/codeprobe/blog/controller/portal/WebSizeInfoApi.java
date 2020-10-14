package cn.codeprobe.blog.controller.portal;

import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.FriendLinkService;
import cn.codeprobe.blog.service.LooperService;
import cn.codeprobe.blog.service.WebSizeInfoAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/web_size_info")
public class WebSizeInfoApi {

    @Autowired
    private WebSizeInfoAdminService webSizeInfoAdminService;
    @Autowired
    private LooperService looperService;
    @Autowired
    private FriendLinkService friendLinkService;


    @GetMapping("/title")
    public ResponseResult getTitle() {
        return webSizeInfoAdminService.getTitle();
    }

    @GetMapping("/view_count")
    public ResponseResult getViewCount() {
        return webSizeInfoAdminService.getViewCount();
    }

    @GetMapping("/seo")
    public ResponseResult getSeo() {
        return webSizeInfoAdminService.getSeoInfo();
    }

    @GetMapping("/looper")
    public ResponseResult getLoops(@RequestParam(value = "page", required = false) String page,
                                   @RequestParam(value = "size", required = false) String size) {
        return looperService.listLoops(page, size);
    }

    @GetMapping("/friend_link")
    public ResponseResult getFriendLinks(@RequestParam(value = "page", required = false) String page,
                                         @RequestParam(value = "size", required = false) String size) {
        return friendLinkService.listFriendLinks(page, size);
    }
}
