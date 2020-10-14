package cn.codeprobe.blog.controller.admin;

import cn.codeprobe.blog.intercept.CheckRepeatCommit;
import cn.codeprobe.blog.pojo.Looper;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.LooperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/looper")
public class LooperApi {

    @Autowired
    private LooperService looperService;

    @PreAuthorize("@permission.admin()")
    @CheckRepeatCommit
    @PostMapping
    public ResponseResult addLooper(@RequestBody Looper looper) {
        return looperService.addLoop(looper);
    }

    @PreAuthorize("@permission.admin()")
    @DeleteMapping("/{looperId}")
    public ResponseResult deleteLooper(@PathVariable("looperId") String looperId) {
        return looperService.deleteLoop(looperId);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/{looperId}")
    public ResponseResult getLooper(@PathVariable("looperId") String looperId) {
        return looperService.getLoop(looperId );
    }

    @PreAuthorize("@permission.admin()")
    @PutMapping("/{looperId}")
    public ResponseResult updateLooper(@PathVariable("looperId") String looperId,@RequestBody Looper looper) {
        return looperService.updateLoop(looperId,looper);
    }

    @PreAuthorize("@permission.admin()")
    @GetMapping("/list/{page}/{size}")
    public ResponseResult listLoopers(@PathVariable("page") String page, @PathVariable("size") String size) {
        return looperService.listLoops(page,size);
    }

}
