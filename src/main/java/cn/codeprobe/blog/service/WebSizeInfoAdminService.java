package cn.codeprobe.blog.service;

import cn.codeprobe.blog.response.ResponseResult;

public interface WebSizeInfoAdminService {
    ResponseResult getTitle();

    ResponseResult updateTitle(String title);

    ResponseResult getSeoInfo();

    ResponseResult updateSeoInfo(String keyword, String description);

    ResponseResult getViewCount();
}
