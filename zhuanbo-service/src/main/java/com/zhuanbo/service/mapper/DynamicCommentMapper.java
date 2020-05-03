package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuanbo.core.entity.DynamicComment;
import com.zhuanbo.service.vo.DynamicCommentAdminVO;
import com.zhuanbo.service.vo.DynamicCommentVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 动态评论表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-04-08
 */
public interface DynamicCommentMapper extends BaseMapper<DynamicComment> {


    @Select("<script> select dc.id,dc.pid,dc.dynamic_id,dc.content,dc.from_uid,u.nickname as fromUserName,u2.nickname as toUserName,u.head_img_url," +
            " dc.add_time from shop_dynamic_comment dc left join shop_user u on dc.from_uid = u.id left join shop_user u2 on dc.to_uid = u2.id  " +
            " where dc.dynamic_id = #{dynamicId} and dc.deleted = 0 " +
            "<if test='commentId != null'>  and dc.id = #{commentId}</if>" +
            " order by dc.add_time desc</script>")
    List<DynamicCommentVO> list(IPage<DynamicCommentVO> page, @Param("dynamicId") Integer dynamicId, @Param("commentId") Integer commentId);

    /**
     * 管理后台
     * @param page
     * @return
     */
    @Select("<script>" +
            " select c.id id,u.user_name,c.content,d.content dynamic_content,c.add_time,d.id dynamic_id,c.update_time,c.deleted," +
            " c.checked, c.operator" +
            " from shop_dynamic_comment c" +
            " left join shop_user u on u.id = c.from_uid" +
            " left join shop_dynamic d on d.id = c.dynamic_id" +
            " where 1 = 1" +
            " <if test='map.ids != null'> and c.id in (${map.ids})</if>" +
            " <if test='map.id != null'> and c.id = #{map.id}</if>" +
            " <if test='map.content != null'> and c.content like #{map.content}</if>" +
            " <if test='map.checked != null'> and c.checked = #{map.checked}</if>" +
            " order by add_time desc</script>")
    List<DynamicCommentAdminVO> adminList(IPage<DynamicCommentAdminVO> page, @Param("map") Map<String, Object> map);
}
