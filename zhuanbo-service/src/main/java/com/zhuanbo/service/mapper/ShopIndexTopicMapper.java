package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.IndexTopic;
import com.zhuanbo.service.vo.TopicsVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-03-14
 */
public interface ShopIndexTopicMapper extends BaseMapper<IndexTopic> {


//    @Select("<script> select t.*,tg.`goods_id` from `shop_index_topic` t left join `shop_index_topic_goods` tg on t.id = tg.`index_topic_id` " +
//            "where <if test='type != 0'> t.type = #{type} </if>  <if test='goodsId != 0'> and tg.goods_id = #{goodsId} </if></script>")

    @Select({"<script>",
            "select t.*,tg.`goods_id` from `shop_index_topic` t left join `shop_index_topic_goods` tg on t.id = tg.`index_topic_id` ",
            "WHERE 1=1",
            "<when test='type!=null'>",
            "AND t.type = #{type}",
            "</when>",
            "<when test='goodsId!=null'>",
            "AND tg.goods_id = #{goodsId}",
            "</when>",
            "group by t.id",
            "</script>"})
    List<TopicsVO> listTopics(Page<TopicsVO> page, @Param("type") String type, @Param("goodsId") String goodsId);

    @Select({"<script>",
            "select count(1) from `shop_index_topic` t ",
            "WHERE 1=1",
            "<when test='type!=null'>",
            "AND t.type = #{type}",
            "</when>",
            "</script>"})
    Integer getTopicsTotal(@Param("type") String type, @Param("goodsId") String goodsId);
}
