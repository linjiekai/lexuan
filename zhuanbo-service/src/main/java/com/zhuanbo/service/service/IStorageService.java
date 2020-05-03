package com.zhuanbo.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhuanbo.core.entity.Storage;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 文件存储表 服务类
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface IStorageService extends IService<Storage> {

    /**
     * 根据文件名获取文件存放的位置
     * @param fileName
     * @return
     */
    String getOSSPath(String fileName);

    /**
     * @Description(描述): 获取oss资源
     * @auther: Jack Lin
     * @param :[url, token, response]
     * @return :void
     * @date: 2019/9/27 15:47
     */
    void ossResouces(String url, String token, HttpServletResponse response) throws  Exception;

    /**
     * @Description(描述): 获取图片-内部系统用，无须验签无需登录
     * @auther: Jack Lin
     * @param :[url, response]
     * @return :void
     * @date: 2019/10/16 17:33
     */
    void ossResoucesForInterior(String url, HttpServletResponse response) throws  Exception;
}
