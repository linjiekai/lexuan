package com.zhuanbo.core.entity.enumeration;

public enum ActionEnum {
    REGISTER("register",1,"消息模版"),LOGIN("login",2,"消息模版"),
    WXLOGIN("wxlogin",3,"消息模版"),CODELOGIN("login",4,"消息模版"),
    FINDPSW("findpsw",5,"消息模版"),MPLOGIN("mpLogin",6,"小程序验证码"),
    INTERIORLOGIN("interiorLogin",7,"内部登录");;

    private  String actionName;
    private  Integer type;
    private  String msg;

    private ActionEnum(String actionName, Integer type , String msg){
        this.actionName = actionName;
        this.type = type;
        this.msg = msg;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static ActionEnum getByTpye(int value) {
        for (ActionEnum actionEnum : values()) {
            if (actionEnum.getType() == value) {
                return actionEnum;
            }
        }
        return null;
    }

    public static String getEnName(Integer type){
        for (ActionEnum actionEnum : values()) {
            if (actionEnum.getType().equals(type)) {
                return actionEnum.getActionName();
            }
        }
        return "";
    }
}
