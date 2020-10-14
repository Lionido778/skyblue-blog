package cn.codeprobe.blog.service;

import cn.codeprobe.blog.pojo.FriendLink;
import cn.codeprobe.blog.response.ResponseResult;

public interface FriendLinkService {

    ResponseResult addFriendLink(FriendLink friendLink);

    ResponseResult getFriendLink(String friendLinkId);

    ResponseResult listFriendLinks(String page, String size);

    ResponseResult updateFriendLink(String friendLinkId, FriendLink friendLink);

    ResponseResult deleteByUpdateState(String friendLinkId);
}
