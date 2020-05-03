package com.zhuanbo.core.dto;

import lombok.Data;

@Data
public class AdminStorageDTO extends AdminBaseRequestDTO {

    String type;
    String fileName;
}
