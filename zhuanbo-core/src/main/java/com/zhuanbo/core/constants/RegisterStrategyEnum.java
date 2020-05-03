package com.zhuanbo.core.constants;

public enum RegisterStrategyEnum {

    NORMAL_REGISTER_STRATEGY_SERVICE_IMPL("NormalRegisterStrategyServiceImpl"),
    WXBIND_REGISTER_STRATEGY_SERVICE_IMPL("NormalRegisterStrategyServiceImpl"),
    MP_REGISTER_STRATEGY_SERVICE_IMPL("NormalRegisterStrategyServiceImpl");

    private String value;
    RegisterStrategyEnum(String value) {
        this.value = value;
    }
    public String value() {
        return this.value;
    }
}
