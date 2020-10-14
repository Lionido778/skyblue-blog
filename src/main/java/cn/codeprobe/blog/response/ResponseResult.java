package cn.codeprobe.blog.response;

public class ResponseResult {

    private int code;
    private boolean success;
    private String message;
    private Object data;

    public ResponseResult(ResponseState responseState) {
        this.code = responseState.getCode();
        this.success = responseState.isSuccess();
        this.message = responseState.getMessage();
    }

    public static ResponseResult SUCCESS() {
        return new ResponseResult(ResponseState.SUCCESS);
    }

    public static ResponseResult SUCCESS(String message) {
        ResponseResult success = SUCCESS();
        success.setMessage(message);
        return success;
    }

    public static ResponseResult REGISTER_SUCCESS() {
        return new ResponseResult(ResponseState.REGISTER_SUCCESS);
    }

    public static ResponseResult LOGIN_SUCCESS() {
        return new ResponseResult(ResponseState.LOGIN_SUCCESS);
    }


    public static ResponseResult FAILED() {
        return new ResponseResult(ResponseState.FAILED);
    }

    public static ResponseResult FAILED(String message) {
        ResponseResult failed = FAILED();
        failed.setMessage(message);
        return failed;
    }

    public static ResponseResult NOT_LOGIN() {
        return new ResponseResult(ResponseState.NOT_LOGIN);
    }

    public static ResponseResult PERMISSION_DENIED() {
        return new ResponseResult(ResponseState.PERMISSION_DENIED);
    }

    public static ResponseResult LOGIN_FAILED() {
        return new ResponseResult(ResponseState.LOGIN_FAILED);
    }

    public static ResponseResult ACCOUNT_DENIED() {
        return new ResponseResult(ResponseState.ACCOUNT_DENIED);
    }

    public static ResponseResult ERROR_403() {
        return new ResponseResult(ResponseState.ERROR_403);
    }

    public static ResponseResult ERROR_404() {
        return new ResponseResult(ResponseState.ERROR_404);
    }

    public static ResponseResult ERROR_504() {
        return new ResponseResult(ResponseState.ERROR_504);
    }

    public static ResponseResult ERROR_505() {
        return new ResponseResult(ResponseState.ERROR_505);
    }

    public static ResponseResult WAiTING_FOR_SCAN() {
        return new ResponseResult(ResponseState.WAiTING_FOR_SCAN);
    }

    public static ResponseResult QR_CODE_DEPRECATE() {
        return new ResponseResult(ResponseState.QR_CODE_DEPRECATE);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public ResponseResult setData(Object data) {
        this.data = data;
        return this;
    }
}
