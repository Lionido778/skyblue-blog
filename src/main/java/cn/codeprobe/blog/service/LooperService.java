package cn.codeprobe.blog.service;

import cn.codeprobe.blog.pojo.Looper;
import cn.codeprobe.blog.response.ResponseResult;

public interface LooperService {

    ResponseResult addLoop(Looper looper);

    ResponseResult deleteLoop(String looperId);

    ResponseResult getLoop(String looperId);

    ResponseResult updateLoop(String looperId, Looper looper);

    ResponseResult listLoops(String page, String size);
}
