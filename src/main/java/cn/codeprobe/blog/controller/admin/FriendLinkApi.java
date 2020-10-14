package cn.codeprobe.blog.controller.admin;

import cn.codeprobe.blog.intercept.CheckRepeatCommit;
import cn.codeprobe.blog.pojo.FriendLink;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.FriendLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/friendLink")
public class FriendLinkApi {

    @Autowired
    private FriendLinkService friendLinkService;

    /**
     * 添加友情链接
     * 权限：管理员权限
     * 提交数据：name,logo,url
     *
     * @param friendLink
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @CheckRepeatCommit
    @PostMapping
    public ResponseResult addFriendLink(@RequestBody FriendLink friendLink) {
        return friendLinkService.addFriendLink(friendLink);
    }

    /**
     * 删除友情链接（通过改变状态 "0" ==> 不可用）
     * 权限：管理员权限
     * @param friendLinkId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{friendLinkId}")
    public ResponseResult deleteFriendLink(@PathVariable("friendLinkId") String friendLinkId) {
        return friendLinkService.deleteByUpdateState(friendLinkId);
    }

    /**
     * 获取友情链接
     * 在修改时需要获取友情链接填充弹窗
     * 权限：管理员权限
     *
     * @param friendLinkId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/{friendLinkId}")
    public ResponseResult getFriendLink(@PathVariable("friendLinkId") String friendLinkId) {
        return friendLinkService.getFriendLink(friendLinkId);
    }

    /**
     * 更新友情列表
     * 权限：管理员权限
     *
     * @param friendLinkId
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @PutMapping("/{friendLinkId}")
    public ResponseResult updateFriendLink(@PathVariable("friendLinkId") String friendLinkId, @RequestBody FriendLink friendLink) {
        return friendLinkService.updateFriendLink(friendLinkId, friendLink);
    }

    /**
     * 获取友情链接列表(分页查询)
     * 权限：管理员权限
     *
     * @param page
     * @param size
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listFriendLinks(@RequestParam("page") String page, @RequestParam("size") String size) {
        return friendLinkService.listFriendLinks(page, size);
    }
}
