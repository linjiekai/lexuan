package com.zhuanbo.core.constants;

public enum ConstantsEnum {
    ARANGO_FROM_ID("arango_from_id"),
    ARANGO_TO_ID("arango_to_id"),
    ARANGO_UPDATE_LEVEL_ID("arango_update_level_id"),
    ARANGO_UPDATE_LEVEL("arango_update_level"),
    PRICE("price"),
    DEPOSIT("deposit"),
    LIMIT_OVERSEA("limit_Oversea"),
    DEPOSIT_UPGRADE_600_NUMBER("deposit_upgrade_600_number"),
    CHECK_PARENT_QUICK_UPGRADE("check_parent_quick_upgrade"),
    CHANGE_USER_LEVEL_MAP("change_user_level_map"),
    NEED_REMOVE_CACHE_IDS("need_remove_cache_ids"),
    AD_SCROLL_LIST("adScrollList"),
    NOTIFY_LIST("notifyList"),
    NOTIFY("notify"),
    NOTIFY_BUYER_GIFT("notify_buyer_gift"),
    NOTIFY_BUYER_GIFT_INVITER("notify_buyer_gift_inviter"),
    NOTIFY_BUYER_FOREVER("notify_buyer_forever"),
    NOTIFY_BUYER_FOREVER_INVITER("notify_buyer_forever_inviter"),
    JSON_10000("10000"),
    JSON_CODE("code"),
    JSON_DATA("data"),
    /**1天提现次数*/
    WITHDR_TIMES("withdrTimes"),
    MAX("max"),
    /**商品修改锁*/
    REDIS_GOODS_UPDATE_LOCK("goods_update_lock"),
    /**正式*/
    USER_PT_FORMAL_1(1),
    /**实习*/
    USER_PT_FORMAL_0(0),
    TRADE_CODE_01("01"),
    TRADE_CODE_02("02"),
    TRADE_CODE_08("08"),
    DEPOSIT_BUSI_TYPE_02("02"),// 充值
    DEPOSIT_BUSI_TYPE_04("04"),// 收益
    DEPOSIT_BUSI_TYPE_05("05"),// 保证金
    DEPOSIT_BUSI_TYPE_06("06"),// 会员套餐
    DEPOSIT_BUSI_TYPE_07("07"),// 扣减
    DEPOSIT_BUSI_TYPE_08("08"),// 外部账户充值
    DEPOSIT_BUSI_TYPE_09("09"),// 保证金扣减
    
    /**达人押金充值金额*/
    REACH2EXPER("reach2Exper"),
    /**星人押金充值金额*/
    STAR2EXPER("star2Exper"),
    FOREVER("forever"),
    COUNT("count"),
    ORDER_TYPE_INVEST("invest"),
    ORDER_TYPE_PORT("port"),
    ORDER_TYPE_GOODS("goods"),
    MIDDLE2HIGH_TEAM_DIRECT_MIDDLE_NUMER("middle2high_team_direct_middle_numer"), // 初级运营商升中级运营商：其中含3名直属中级运营商
    MIDDLE2HIGH_TEAM_MIDDLE_NUMER("middle2high_team_middle_numer"),// 中级运营商升高级运营商：团队含10名中级运营商
    LOW2MIDDLE_TEAM_DIRECT_LOW_NUMER("low2middle_team_direct_low_numer"),// 初级运营商升中级运营商：其中含3名直属初级运营商
    LOW2MIDDLE_TEAM_LOW_NUMER("low2middle_team_low_numer"),// 初级运营商升中级运营商：团队含10名初级运营商
    OPERATION("operation"),
    DEPOSIT_UPGRADE_DA_NUMBER("deposit_upgrade_da_number"), // 押金升级下的直属达人数量
    ACTIVITY_UPGRADE_CHECK("check"),
    ACTIVITY_UPGRADE("activityUpgrade"),
    REDIS_INVITE_CODE_LOCK("zhuanbo_invite_code_lock"), // 邀请码的数据的锁
    REDIS_INVITE_CODE_MAX("zhuanbo_invite_code_max"), // 邀请码的数据的最大一位数
    REDIS_INVITE_CODE_SET("zhuanbo_invite_code_set"), // 邀请码的数据
    MOBILE_REG("^1\\d{10}$"), // 手机的校验
    REDIS_INDEX_QUICK("index_quick_list"),
    REDIS_INDEX_TOPIC("index_topic_"),
    REDIS_INDEX_ADS("index_ads_list"),//首页广告
    REDIS_PRIOR_ADS("prior_ads_list"),//优享广告
    REDIS_INDEX_SHOWCATEGORY("index_showcategory_list"),//首页展示分类
    REDIS_USER_MPMALL("user_mpmall_"),
    COMPANY_USER_ID("companyUserId"), // 公司账号
    MALL_USER("mallUser"),// 公司账号
    MQ_RETRY_TIME("xfhl-retry-time"),
    PLANA_TI2SI_PRICE("PlanATi2Si_Price"), // 体升司A：累积业绩1000000
    PLANA_TI2SI_DA_NUMBER("PlanATi2Si_DaNumber"), // 体升司A：1000个M达人官
    PLANA_TI2SI_DIRECT_TI_NUMBER("PlanATi2Si_DirectTiNumber"), // 体升司A：5个直属M体验官
    PLANA_TI2SI_TI_NUMBER("PlanATi2Si_TiNumber"), // 体升司A：10个M体验官
    PLANB_DA2TI_PRICE("PlanBDa2Ti_Price"), // 达升体B：累积业绩60000
    PLANB_DA2TI_DIRECT_DA_NUMBER("PlanBDa2Ti_DirectDaNumber"),// 达升体B：50个直属M达人
    PLANA_DA2TI_PRICE("PlanADa2Ti_Price"), // 达升体A：累积业绩60000
    PLANA_DA2TI_DIRECT_DA_NUMBER("PlanADa2Ti_DirectDaNumber"), // 达升体A：20个直属M达人
    PLANA_DA2TI_DA_NUMBER("PlanADa2Ti_DaNumber"),// 达升体A：团队100个M达人
    LEVEL_SYSTEM("levelSystem"),
    DEPOSIT_ORDER_STATUS_W("W"), // 充值订单：待提现
    DEPOSIT_ORDER_STATUS_S("S"), // 充值订单：提现成功
    DEPOSIT_ORDER_STATUS_BW("BW"), // 充值订单：提现确认
    DEPOSIT_ORDER_STATUS_F("F"), // 充值订单：提现失败
    INCOME_TYPE_1(1),//  1:商品销售奖励
    INCOME_TYPE_2(2),// 2:服务商销售奖励
    INCOME_TYPE_3(3),// 3:下级销售扣减
    INCOME_TYPE_4(4),// 4：提现
    INCOME_TYPE_5(5),// 5:进货差价奖励
    INCOME_TYPE_6(6),// 6:下级运费扣减
    CHANGE_TYPE_1(1),// 1:退款
    CHANGE_TYPE_2(2),// 2:调账
    CHANGE_TYPE_3(3),// 3:奖励
    CHANGE_TYPE_4(4),// 4:提现
    REWARD_TYPE_1(1),// 1:商品销售奖励
    REWARD_TYPE_2(2),// 2:服务商销售奖励
    REWARD_TYPE_3(3),// 3:下级销售扣减
    REWARD_TYPE_4(4),// 4:服务商销售奖励
    PROFIT_TYPE_1(1), // 直推
    PROFIT_TYPE_2(2), // 销售额
	X_MP_SIGNVER_V_1("v1"),
	WEIXIN("WEIXIN"),
	ALIPAY("ALIPAY"),
    WITHDR_ORDER_STATUS_W("W"),// 提现订单状态：待提现
    WITHDR_ORDER_STATUS_S("S"),// 提现订单状态：成功
    PLATFORM_ZBMALL("ZBMALL"),
    REDIS_LEVEL_PARENT("mpwj_level_parent_"),// 层级关系父
    REDIS_LEVEL_CHILDREN("mpwj_level_children_"),// // 层级关系子
    REDIS_LEVEL_PARENT_ALL("mpwj_level_parent_all_"),// 层级关系父所有
    REDIS_BRAND_INDEX("brand_index_"),//品牌信息
    REDIS_BRAND_INDEX_LIST("brand_index_list"),//品牌信息
    
    SHOW_CATEGORY_CHILDREN("show_category_children_"),// 展示分类子集
    SHOW_CATEGORY_PARENT("show_category_parent_"),// // 层级分类父集
    SHOW_CATEGORY_ENTITY("show_category_entity_"),// // 层级分类父集

    CATEGORY_ENTITY("category_entity_"),//商品分类
    
    /** 合伙人等级 0:M星人;1:M达人;2M体验官:;3:M司令*/
    USER_PT_LEVEL_0(0),
    USER_PT_LEVEL_1(1),
    USER_PT_LEVEL_2(2),
    USER_PT_LEVEL_3(3),
    DELETED_0(0),
    DELETED_1(1),
    ROLE_DELETED_0(0),
    ROLE_DELETED_1(1),
    PRODUCT_DELETED_0(0),
    PRODUCT_DELETED_1(1),
    GOODS_STATUS_F_1(-1),
    GOODS_STATUS_0(1),
    GOODS_STATUS_1(1),
    GOODS_STATUS_2(2),
    GOODS_DELETED_0(0),
    GOODS_DELETED_1(1),
    GOODS_TYPE_0(0),// 普通商品
    GOODS_TYPE_1(1),// 会员礼包
    /**第三方推送状态：1*/
    PUSH_TASK_STATUS_1("1"),
    /**第三方推送状态：2*/
    PUSH_TASK_STATUS_2("2"),
    PUSH_CODE("code"),
    PUSH_CODE_0("0"),
    PUSH_CODE_DATA("data"),
    PUSH_ANDROID("android"),
    PUSH_IOS("ios"),
    PUSH_STATUS("status"),
    /**redis.key：areaCode*/
    REDIS_KEY_AREA_CODE("areaCode"),
    /**用户状态：0:待审核*/
    REDIS_LIVE_CHANNEL("liveChannel"),
    /**用户状态：0:待审核*/
    USER_STATUS_0(0),
    /**用户状态：1:正常*/
    USER_STATUS_1(1),
    /**用户状态：2:冻结/黑名单*/
    USER_STATUS_2(2),
    /**性别 0:未知*/
    USER_GENDER_0(0),
    /**性别 1:男*/
    USER_GENDER_1(1),
    /**性别 2:女*/
    USER_GENDER_2(2),
    /**用户绑定第三方状态：1:有效*/
    USER_BIND_THIRD_STATUS_0(0),
    /**用户绑定第三方状态：0:无效*/
    USER_BIND_THIRD_STATUS_1(1),
    /**绑定类型:微信*/
    USER_BIND_THIRD_TYPE_WEIXIN("WEIXIN"),
    /**绑定类型:支付宝*/
    USER_BIND_THIRD_TYPE_ALIPAY("ALIPAY"),
    /**绑定类型:云信*/
    USER_BIND_THIRD_TYPE_YUNXIN("YUNXIN"),
    /**绑定类型:微博*/
    USER_BIND_THIRD_TYPE_WEIBO("WEIBO"),
    /**绑定类型h5*/
    USER_BIND_THIRD_TYPE_H5("H5"),
    /**绑定类型:QQ*/
    USER_BIND_THIRD_TYPE_QQ("QQ"),
    USER_BIND_THIRD_TYPE_WX_APPLET("WX-APPLET"),
    USER_BIND_THIRD_TYPE_WX_PUBLIC("WX_PUBLIC"),
    SHOW_CATEGORY_STATUS_1(1),
    SHOW_CATEGORY_STATUS_2(1),

    /**公共参数*/
    WITHDR_TO_BANK("withdrToBank"),// 是否开放提现到银行卡 0：关闭，1：开放
    QUICK_PAY("quickPay"),// 是否开放银行卡快捷支付 0：关闭，1：开放
    PAY_PLATFORM_GHT("GAOHUITONG"),//支付平台-高汇通
    MP("mp"),
    QRCODE("qrcode"),
    WITHDR_COMMISSION("withdrCommission"),
    CODE_SOURCE_MP("mp"),// code来源小程序
    CODE_SOURCE_GZH("gzh"),// code来源小程序
    MASK_BUSINESS("maskBusiness"),// 面膜业务
    ENTRANCE_OPEN("entrance_open"),// 是否开启面膜业务入口

    ;
    private Object value;

    ConstantsEnum(Object value){
        this.value = value;
    }
    public Object value(){
        return this.value;
    }

    public Integer integerValue() {
        return Integer.valueOf(String.valueOf(this.value));
    }

    public String stringValue() {
        return String.valueOf(this.value);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
    
}
