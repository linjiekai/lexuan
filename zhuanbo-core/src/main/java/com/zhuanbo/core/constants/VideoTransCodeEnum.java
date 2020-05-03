package com.zhuanbo.core.constants;

public enum VideoTransCodeEnum {

    KEY_TRANSCODE("transcode"),
    KEY_VIDEO("video"),
    GOODS("goods"),
    DYNAMIC("dynamic");

    private String value;

    VideoTransCodeEnum(String value){
        this.value = value;
    }

    public String value(){
        return this.value;
    }
}
