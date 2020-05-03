package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuanbo.core.entity.Dynamic;
import com.zhuanbo.service.vo.DynamicVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-04-04
 */
public interface DynamicMapper extends BaseMapper<Dynamic> {


    @Select("<script> " +
            " select d.id,d.content,d.video_url as videoImage, d.like_number,d.add_time,u.nickname,u.head_img_url,d.video_width,d.video_height,d.video_url," +
            " g.name goods_name,g.price goods_price, g.cover_images,d.goods_id,g.side_name goods_side_name, d.video_transcode_url " +
            "<if test='userId != null'>,if(lk.id > 0,1,0) liked </if>" +
            "<if test='userId == null'>,0 liked </if>" +
            " from shop_dynamic d " +
            " left join shop_user u on d.user_id = u.id " +
            " left join shop_goods g on g.id = d.goods_id " +
            "<if test='userId != null'> left join shop_dynamic_like lk on lk.dynamic_id = d.id and lk.user_id = #{userId} </if>" +
            " where d.deleted = 0 and date_format(d.show_time,'%Y-%m-%d-%H-%i-%S') &lt;= date_format(now(),'%Y-%m-%d-%H-%i-%S') order by d.sequence_number desc" +
            "</script>")
    List<DynamicVO> list(IPage<DynamicVO> page, @Param("userId") Integer userId);
    /**
     * 更新点赞数量
     * @param id
     * @param number
     * @return
     */
    //  and like_number + #{number} > -1
    @Update("update shop_dynamic set like_number = like_number + #{number} where id = #{id}")
    int updateLikeNumber(@Param("id") Long id, @Param("number") int number);
}
