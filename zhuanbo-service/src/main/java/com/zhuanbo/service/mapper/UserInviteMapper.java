package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuanbo.core.entity.UserInvite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户邀请关系表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface UserInviteMapper extends BaseMapper<UserInvite> {

    /**
     * 根据 userId 获取直属为 xxx等级 的数量
     * @param userId
     * @param ptLevel 直属的等级
     * @return
     */
    @Select("select count(1) from shop_user u where u.id in (select id from shop_user_invite where pid = #{userId}) and u.pt_level = #{ptLevel}")
    int immediateNumber(@Param("userId") Long userId, @Param("ptLevel") Integer ptLevel);

    /**
     * 某个用户下的直属平级数量
     * @param userId 某个用户
     * @param level 某个用户的平级
     * @return
     */
    @Select("select count(1) from shop_user_invite ui inner join shop_user u on u.id = ui.id where pid = #{userId} and u.pt_level = #{ptLevel}")
    int directLevelUserNumber(@Param("userId") Long userId, @Param("ptLevel") Integer level);

    @Select("select * from shop_user_invite")
    List<UserInvite> xxx();
}
