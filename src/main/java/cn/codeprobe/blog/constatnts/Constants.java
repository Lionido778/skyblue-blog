package cn.codeprobe.blog.constatnts;

public interface Constants {

    interface Terminal {
        String TERMINAL_PC = "pc";
        String TERMINAL_MOBILE = "mobile";
    }

    interface Settings {
        String INIT_MANAGER_ACCOUNT = "init_manager_account";
        String INIT_MANAGER_ACCOUNT_VALUE = "1";
        String WEB_SIZE_INFO_TITLE = "web_size_info_title";
        String WEB_SIZE_INFO_KEYWORDS = "web_size_info_keywords";
        String WEB_SIZE_INFO_DESCRIPTION = "web_size_info_description";
        String WEB_SIZE_INFO_VIEW_COUNT = "web_size_info_view_count";
    }

    interface User {
        String ROLE_ADMIN = "role_admin";
        String ROLE_NORMAL = "role_normal";
        String DEFAULT_AVATAR = "https://s2.ax1x.com/2020/01/06/lyNr6A.jpg";
        String DEFAULT_SIGN = "这个家伙有点懒,什么都没有留下...";
        String DEFAULT_ADMIN_STATE = "1";
        String DEFAULT_NORMAL_STATE = "1";
        //用户注册方式
        String REGISTER_BY_PHONE = "phone";
        String REGISTER_BY_EMAIL = "email";
        //图灵验证码-redis
        String KEY_CAPTCHA_CODE = "key_captcha_code_";
        //邮箱验证码-redis
        String KEY_EMAIL_CODE = "key_email_code_";
        String KEY_EMAIL_IP_SEND_CODE = "key_email_ip_send_code_";
        String KEY_EMAIL_ADDR_SEND_CODE = "key_email_addr_send_code_";
        //短信验证码-redis
        String KEY_SMS_PHONE_NUMBERS = "17789445253";
        String KEY_SMS_SIGN_NAME = "蔚蓝博客";
        String KEY_SMS_TEMPLATE_CODE = "SMS_198667747";
        String KEY_SMS_CODE = "key_sms_code_";
        //token-redis
        String KEY_TOKEN = "key_token_";
        //重复提交-redis
        String KEY_REPEAT_COMMIT = "key_commit_repeat_commit_";
        //二维码登录
        String KEY_QR_CODE = "key_qr_code_";
        String QR_CODE_LOGIN_FAILED = "false";
        int QR_CODE_STATE_CHECK_WAITING_TIME = 30;
        String KEY_REPEAT_ACQUIRE_QR_CODE = "key_repeat_acquire_qr_code";
    }

    interface Cookie {
        String COOKIE_TOKEN_KEY = "blog_system";
        String COOKIE_QR_CODE = "blog_system_acquired_qr_code";
    }

    interface Page {
        int DEFAULT_PAGE = 1;
        int DEFAULT_SIZE = 2;
        //分页page,size没有参数为null时
        String MAX_PAGE = "1";
        String MAX_SIZE = "15";
    }

    // 单位是 seconds 秒
    interface TimeValueInSeconds {
        int MIN = 60;
        int HOUR = 60 * MIN;
        int HOUR_2 = 2 * HOUR;
        int DAY = 24 * HOUR;
        int WEEK = 7 * DAY;
        int MONTH = 30 * DAY;
    }

    // 单位是 millions 毫秒
    interface TimeValueInMillions {
        long MIN = 60 * 1000;
        long HOUR = 60 * MIN;
        long HOUR_2 = 2 * HOUR;
        long DAY = 24 * HOUR;
        long WEEK = 7 * DAY;
        long MONTH = 30 * DAY;
    }

    // image
    interface ImageType {
        String TYPE_PNG = "png";
        String TYPE_GIF = "gif";
        String TYPE_JPG = "jpg";
        String TYPE_PNG_WITH_PREFIX = "image/png";
        String TYPE_GIF_WITH_PREFIX = "image/gif";
        String TYPE_JPG_WITH_PREFIX = "image/jpeg";
    }

    // article
    interface Article {
        //限制条件
        int TITLE_MAX_LENGTH = 128;
        int SUMMARY_MAX_LENGTH = 256;
        //文章状态
        String STATE_DELETE = "0";
        String STATE_PUBLISH = "1";
        String STATE_DRAFT = "2";
        String STATE_TOP = "3";
        //内容格式
        String TYPE_RICH_TEXT = "0";
        String TYPE_MARKDOWN = "1";
        //redis
        String CACHE_ARTICLE = "cache_article_";
        String CACHE_VIEW_COUNT = "cache_view_count_";
        String CACHE_FIRST_PAGE_ARTICLE = "cache_first_page_article";
    }

    // Comment
    interface Comment {
        String STATE_PUBLISH = "1";
        String STATE_TOP = "3";
        //缓存
        String CACHE_FIRST_PAGE_COMMENT = "cache_first_page_comment_";
    }
}
