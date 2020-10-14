package cn.codeprobe.blog.service.impl;

import cn.codeprobe.blog.dao.SettingDao;
import cn.codeprobe.blog.pojo.Setting;
import cn.codeprobe.blog.response.ResponseResult;
import cn.codeprobe.blog.service.WebSizeInfoAdminService;
import cn.codeprobe.blog.constatnts.Constants;
import cn.codeprobe.blog.utils.id.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Transactional
@Service
public class WebSizeInfoAdminServiceImpl implements WebSizeInfoAdminService {

    @Autowired
    private SettingDao settingDao;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;


    @Override
    public ResponseResult getTitle() {
        Setting dbTitle = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_INFO_TITLE);
        if (dbTitle == null) {
            return ResponseResult.FAILED("网站标题获取失败");
        }
        return ResponseResult.SUCCESS("标题获取成功").setData(dbTitle.getValue());
    }

    @Override
    public ResponseResult updateTitle(String title) {
        Setting dbTitle = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_INFO_TITLE);
        if (dbTitle == null) {
            dbTitle = new Setting();
            dbTitle.setId(snowflakeIdWorker.nextId() + "");
            dbTitle.setKey(Constants.Settings.WEB_SIZE_INFO_TITLE);
            dbTitle.setCreateTime(new Date());
            dbTitle.setUpdateTime(new Date());
        }
        dbTitle.setValue(title);
        dbTitle.setUpdateTime(new Date());
        settingDao.save(dbTitle);
        return ResponseResult.SUCCESS("更新成功").setData(dbTitle);
    }

    @Override
    public ResponseResult getSeoInfo() {
        Setting dbKeyword = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_INFO_KEYWORDS);
        if (dbKeyword == null) {
            return ResponseResult.FAILED("关键字获取失败");
        }
        Setting dbDescription = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_INFO_DESCRIPTION);
        if (dbDescription == null) {
            return ResponseResult.FAILED("网站描述获取失败");
        }
        Map<String, Object> result = new HashMap<>();
        result.put(dbKeyword.getKey(), dbKeyword.getValue());
        result.put(dbDescription.getKey(), dbDescription.getValue());
        return ResponseResult.SUCCESS("SEO信息获取成功").setData(result);
    }

    @Override
    public ResponseResult updateSeoInfo(String keyword, String description) {
        Setting dbKeyword = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_INFO_KEYWORDS);
        if (dbKeyword == null) {
            dbKeyword = new Setting();
            dbKeyword.setId(snowflakeIdWorker.nextId() + "");
            dbKeyword.setKey(Constants.Settings.WEB_SIZE_INFO_KEYWORDS);
            dbKeyword.setCreateTime(new Date());
            dbKeyword.setUpdateTime(new Date());
        }
        Setting dbDescription = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_INFO_DESCRIPTION);
        if (dbDescription == null) {
            dbDescription = new Setting();
            dbDescription.setId(snowflakeIdWorker.nextId() + "");
            dbDescription.setKey(Constants.Settings.WEB_SIZE_INFO_DESCRIPTION);
            dbDescription.setCreateTime(new Date());
            dbDescription.setUpdateTime(new Date());
        }
        dbKeyword.setValue(keyword);
        dbKeyword.setUpdateTime(new Date());
        settingDao.save(dbKeyword);
        dbDescription.setValue(description);
        dbDescription.setUpdateTime(new Date());
        settingDao.save(dbDescription);
        return ResponseResult.SUCCESS("SEO更新成功");
    }

    @Override
    public ResponseResult getViewCount() {
        Setting dbViewCount = settingDao.findOneByKey(Constants.Settings.WEB_SIZE_INFO_VIEW_COUNT);
        if (dbViewCount == null) {
            dbViewCount = new Setting();
            dbViewCount.setId(snowflakeIdWorker.nextId() + "");
            dbViewCount.setKey(Constants.Settings.WEB_SIZE_INFO_VIEW_COUNT);
            dbViewCount.setCreateTime(new Date());
            dbViewCount.setUpdateTime(new Date());
            dbViewCount.setValue("1");
        }
        int value = Integer.parseInt(dbViewCount.getValue());
        return ResponseResult.SUCCESS("网站浏览量获取成功").setData(value);
    }
}
