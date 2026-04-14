package com.dianjing.common;

public class Constants {
    // 用户角色
    public static final int ROLE_USER = 0;
    public static final int ROLE_PROVIDER = 1;
    public static final int ROLE_ADMIN = 2;

    // 用户状态
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_DISABLED = 1;

    // 订单状态
    public static final int ORDER_PENDING_PAYMENT = 0;
    public static final int ORDER_PENDING_SERVICE = 1;
    public static final int ORDER_IN_SERVICE = 2;
    public static final int ORDER_PENDING_REVIEW = 3;
    public static final int ORDER_COMPLETED = 4;
    public static final int ORDER_CANCELLED = 5;
    public static final int ORDER_REFUNDING = 6;
    public static final int ORDER_REFUNDED = 7;

    // 服务类型
    public static final int SERVICE_TYPE_COMPANION = 0;
    public static final int SERVICE_TYPE_BOOSTING = 1;

    // 服务状态
    public static final int SERVICE_PENDING_REVIEW = 2;
    public static final int SERVICE_REJECTED = 3;
    public static final int SERVICE_ONLINE = 1;
    public static final int SERVICE_OFFLINE = 0;

    // 消息类型
    public static final int MESSAGE_TYPE_SYSTEM = 0;
    public static final int MESSAGE_TYPE_CHAT = 1;
    public static final int MESSAGE_TYPE_PRICE_REQUEST = 2;
    public static final int MESSAGE_TYPE_PRICE_RESPONSE = 3;

    // 支付类型
    public static final int PAYMENT_TYPE_PAY = 0;
    public static final int PAYMENT_TYPE_RECHARGE = 1;
    public static final int PAYMENT_TYPE_WITHDRAW = 2;
    public static final int PAYMENT_TYPE_WITHDRAWAL = 2;
    public static final int PAYMENT_TYPE_INCOME = 3;
    public static final int PAYMENT_TYPE_REFUND = 4;
    public static final int PAYMENT_TYPE_PLATFORM_FEE = 5;
    public static final int PAYMENT_TYPE_PENALTY = 6;

    // 支付状态
    public static final int PAYMENT_STATUS_PENDING = 0;
    public static final int PAYMENT_STATUS_SUCCESS = 1;
    public static final int PAYMENT_STATUS_FAILED = 2;

    // 提现状态
    public static final int WITHDRAWAL_STATUS_PENDING = 0;
    public static final int WITHDRAWAL_STATUS_APPROVED = 1;
    public static final int WITHDRAWAL_STATUS_REJECTED = 2;
    public static final int WITHDRAWAL_STATUS_COMPLETED = 3;

    // 公告状态
    public static final int ANNOUNCEMENT_UNPUBLISHED = 0;
    public static final int ANNOUNCEMENT_PUBLISHED = 1;

    // 游戏类型
    public static final String[] GAME_TYPES = {"LOL", "DOTA2", "CSGO", "王者荣耀", "和平精英", "原神", "永劫无间", "APEX"};

    // 平台手续费比例
    public static final double PLATFORM_FEE_RATE = 0.1;
    public static final double MIN_WITHDRAWAL_AMOUNT = 50.0;

    // 投诉状态
    public static final int COMPLAINT_PENDING = 0;
    public static final int COMPLAINT_PROCESSING = 1;
    public static final int COMPLAINT_RESOLVED = 2;
    public static final int COMPLAINT_REJECTED = 3;

    // 投诉类型
    public static final int COMPLAINT_TYPE_SERVICE_QUALITY = 0;
    public static final int COMPLAINT_TYPE_LATE_DELIVERY = 1;
    public static final int COMPLAINT_TYPE_FRAUD = 2;
    public static final int COMPLAINT_TYPE_OTHER = 3;

    // 活动类型
    public static final int ACTIVITY_TYPE_GLOBAL_DISCOUNT = 0;
    public static final int ACTIVITY_TYPE_SERVICE_DISCOUNT = 1;
    public static final int ACTIVITY_TYPE_SERVICE_SPECIAL_PRICE = 2;

    // 活动状态
    public static final int ACTIVITY_STATUS_NOT_STARTED = 0;
    public static final int ACTIVITY_STATUS_ACTIVE = 1;
    public static final int ACTIVITY_STATUS_ENDED = 2;
}
