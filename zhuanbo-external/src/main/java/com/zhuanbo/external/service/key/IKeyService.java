package com.zhuanbo.external.service.key;


import java.util.Map;

import com.zhuanbo.external.service.dto.AppIdKeyDTO;


public interface IKeyService {
    Map<String, Object> param(AppIdKeyDTO appIdKeyDTO);
}
