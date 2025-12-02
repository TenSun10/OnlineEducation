package com.tenxi.enums;


import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum ErrorCode {
    SERVER_INNER_ERROR(500, "服务器内部未知错误"),

    // 用户相关
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已禁用"),
    USER_RESET_ERROR(1003, "用户已存在"),
    CODE_VALID_FAILED(1004, "验证码错误"),
    CODE_IS_NULL(1005, "验证码为空"),
    JWT_NOT_FOUND(1006, "缺少身份验证头部信息"),
    JWT_INVALID_FAILED(1007, "JWT解析异常"),
    JWT_OUT_OF_DATE(1008, "用户登录信息过期"),
    SIGN_IN_DUPLI_OR_ERROR(1009, "重复打卡或打卡失败"),


    //课程相关
    COURSE_NOT_FOUND(2001, "课程不存在"),
    COURSE_DEL_FAILED(2002, "课程删除失败"),
    COURSE_NOT_AUTH(2003,"无权限操作该视频"),
    COURSE_PUBLISH_FAILED(2004, "课程发布失败"),
    COURSE_UPDATE_FAILED(2005, "课程更新失败"),
    SEARCH_FAILED(2006, "搜索服务异常"),

    //订单相关
    ORDER_EXPIRED(2101, "订单已过期"),
    ORDER_NOT_FOUND(2102, "订单不存在"),
    ORDER_STATUS_UPDATE_FAILED(2103, "订单状态修改失败"),
    ORDER_DEL_FAILED(2104, "订单删除成功"),

    // 分类相关
    CATEGORY_ALREADY_EXISTS(2201, "分类已存在"),
    CATEGORY_SAVE_FAILED(2202, "分类保存失败"),
    CATEGORY_NOT_FOUND(2203, "分类不存在"),
    CATEGORY_STATUS_UPDATE_FAILED(2204, "分类状态更新失败"),


    // 支付相关
    PAYMENT_FAILED(2301, "支付失败"),
    PAYMENT_SIGNATURE_INVALID(2302, "支付签名验证失败"),
    PAYMENT_DETAIL_SET_FAILED(2303, "创建支付详情失败"),
    PAYMENT_SUBSCRIBE_SET_FAILED(2304,"支付后创建订阅详情失败"),

    // 评论相关
    COMMENT_NOT_FOUND(2401, "未找到相关评论"),
    COMMENT_NOT_AUTH(2402, "无权限操作该评论"),
    COMMENT_PUBLISH_FAILED(2403, "评论发布失败"),

    // 通知相关
    NOTIFY_TYPE_ADD_FAILED(2501, "新增通知类型失败"),
    NOTIFY_TYPE_NOT_FOUND(2502, "通知类型未找到"),
    NOTIFY_NOT_FOUND(2503, "通知不存在"),

    //数据库相关
    DATA_INTEGRITY_VIOLATION(3001, "数据完整性异常"),
    DATABASE_ERROR(3002, "数据库访问异常"),

    // 参数校验
    PARAM_INVALID(4001, "参数无效"),
    PARAM_MISSING(4002, "参数缺失"),

    // Feign相关
    FEIGN_PARAM_INVALID(5001, "服务调用参数错误"),
    FEIGN_AUTH_FAIL(5002, "服务调用未授权"),
    FEIGN_SERVICE_NOT_FOUND(5003, "服务调用不存在"),
    FEIGN_SERVICE_ERROR(5004, "服务调用内部错误"),
    FEIGN_SERVICE_UNAVAILABLE(5005, "服务不可用"),

    //文件相关
    FILE_UPLOAD_FAILED(6001, "文件上传失败"),
    FILE_EMPTY(6002, "文件不能为空"),
    FILE_TOO_LARGE(6003, "文件大小超出限制"),
    FILE_TYPE_NOT_SUPPORTED(6004, "文件类型不支持"),
    OSS_INIT_FAILED(6005, "OSS初始化失败"),
    FILE_RENAME_FAILED(6006, "文件重命名失败"),
    FILE_NOT_FOUND(6007, "文件不存在"),

    //视频播放相关
    VIDEO_PROGRESS_NOT_FOUND(7001, "未找到视频进度记录"),
    VIDEO_WATCH_HISTORY_NOT_FOUND(7002, "未找到视频观看记录"),
    VIDEO_WATCH_HISTORY_OPERATION_NOT_AUTH(7003, "无权限操作该观看记录");

    private final int code;
    private final String message;

    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }
}
