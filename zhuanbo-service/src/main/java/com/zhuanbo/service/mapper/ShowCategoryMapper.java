package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.ShowCategory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 展示类目表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-07-03
 */
public interface ShowCategoryMapper extends BaseMapper<ShowCategory> {

    /**
     * @param :[pid]
     * @return :java.util.List<com.zhuanbo.core.entity.ShowCategory>
     * @Description(描述): 获取所有delete=0的子节点
     * @auther: Jack Lin
     * @date: 2019/7/12 16:17
     */
    @Select("<script> select id,pid,name,level,indexs,icon_url,show_url,product_url,status,enable "
    		+ " from shop_show_category where deleted=0 "
    		+ " <if test='params.pid != null'> and pid = #{params.pid} </if> "
    		+ " <if test='params.status != null'> and status = #{params.status} </if> "
    		+ " order by indexs desc "
    		+ "</script> " 
    		)
    List<ShowCategory> getChildren(@Param("params") Map<String, Object> params);

	@Select("SELECT id from shop_show_category t, (SELECT @DATAS := getChildId_from_showCategory (#{pid})) a WHERE find_in_set (ID, @DATAS);")
    List<Long> selectChildrenIds(Long pid);
}
