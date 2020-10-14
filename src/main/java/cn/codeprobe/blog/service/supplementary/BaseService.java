package cn.codeprobe.blog.service.supplementary;

import cn.codeprobe.blog.constatnts.Constants;
import cn.codeprobe.blog.utils.common.StringUtil;

public class BaseService {
    public int checkPage(int page) {
        if (page < Constants.Page.DEFAULT_PAGE) {
            page = Constants.Page.DEFAULT_PAGE;
        }
        return page;
    }

    public int checkSize(int size) {
        if (size < Constants.Page.DEFAULT_SIZE) {
            size = Constants.Page.DEFAULT_SIZE;
        }
        return size;
    }

    public int checkPage(String pageStr) {
        int pageNum = 0;
        if (StringUtil.isEmpty(pageStr)) {
            pageStr = Constants.Page.MAX_PAGE;
            pageNum = Integer.parseInt(pageStr);
            return pageNum;
        }
        pageNum = Integer.parseInt(pageStr);
        int page = checkPage(pageNum);
        return page;
    }

    public int checkSize(String sizeStr) {
        int sizeNum = 0;
        if (StringUtil.isEmpty(sizeStr)) {
            sizeStr = Constants.Page.MAX_SIZE;
            sizeNum = Integer.parseInt(sizeStr);
            return sizeNum;
        }
        sizeNum = Integer.parseInt(sizeStr);
        int size = checkSize(sizeNum);
        return size;
    }

}
