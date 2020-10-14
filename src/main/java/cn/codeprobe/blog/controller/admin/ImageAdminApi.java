package cn.codeprobe.blog.controller.admin;

import cn.codeprobe.blog.intercept.CheckRepeatCommit;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/image")
public class ImageAdminApi {

    @Autowired
    private ImageService imageService;

    /**
     * 关于图片（文件）上传
     * 一般来说，现在比较常用的是对象存储--->很简单，看文档就可以学会了
     * 使用 Nginx + fastDFS == > fastDFS -- > 处理文件上传， Nginx -- > 负责处理文件访问
     *
     * @param file
     * @return
     */
    @PreAuthorize("@permission.admin()")
    @CheckRepeatCommit
    @PostMapping
    public ResponseResult uploadImage(@RequestBody MultipartFile file) {
        return imageService.uploadImage(file);
    }

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{imageId}")
    public ResponseResult deleteImage(@PathVariable("imageId") String imageId) {
        return imageService.deleteImageByUpdateState(imageId);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list")
    public ResponseResult listImages(@RequestParam("page") int page, @RequestParam("size") int size) {
        return imageService.listImages(page,size);
    }
}
