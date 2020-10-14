package cn.codeprobe.blog.response;

public enum ResponseState {
    SUCCESS(20000, true, "操作成功"),
    REGISTER_SUCCESS(20001, true, "注册成功"),
    LOGIN_SUCCESS(20002, true, "登录成功"),
    FAILED(40000, false, "操作失败"),
    NOT_LOGIN(40001, false, "账号未登录"),
    PERMISSION_DENIED(40002, false, "权限不够"),
    LOGIN_FAILED(40003, false, "登录失败"),
    ACCOUNT_DENIED(4004, false, "账户被封禁"),

    ERROR_403(40005, false, "权限不足"),
    ERROR_404(40006, false, "页面丢失"),
    ERROR_504(40007, false, "系统繁忙，请稍后重试"),
    ERROR_505(40008, false, "请求错误，检查所提交数据"),

    WAiTING_FOR_SCAN(40009, false, "等待扫码"),
    QR_CODE_DEPRECATE(40010, false, "二维码已过期");


    private int code;
    private String message;
    private boolean success;

    ResponseState(int code, boolean success, String message) {
        this.code = code;
        this.message = message;
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }
}
