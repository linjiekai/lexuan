package com.zhuanbo.client.server.dto;

import lombok.Data;

@Data
public class ResponseDTO {

    String msg;
    String code;
    Object data;
}
