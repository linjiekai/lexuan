package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuanbo.core.dto.AdminRealNameDTO;
import com.zhuanbo.core.dto.WxUserDTO;
import com.zhuanbo.core.entity.User;
import com.zhuanbo.service.vo.UserVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("<script> select u.*,ui2.pid from shop_user u left join (select count(1) number, id from shop_user_invite group by id) ui" +
            " on ui.id = u.id left join shop_user_invite ui2  on u.id = ui2.id  " +
            " where 1 = 1" +
            "<if test='ew != null and ew.listType == 1'> and u.pt_level &gt; 0 </if>" +
            "<if test='ew != null and ew.id != null'> and u.id = #{ew.id}</if>" +
            "<if test='ew != null and ew.mobile != null'> and u.mobile like #{ew.mobile}</if>" +
            "<if test='ew != null and ew.ptLevel != null'> and u.pt_level = #{ew.ptLevel}</if>" +
            "<if test='ew != null and ew.pid != null'> and ui2.pid = #{ew.pid}</if>" +
            "<if test='ew != null and ew.inviteNumber != null'> and ui.number &gt;= #{ew.inviteNumber}</if>" +
            "<if test='ew != null and ew.nickname != null'> and u.nickname like #{ew.nickname}</if>" +
            " order by u.id desc </script>")
    List<UserVO> pageManual(IPage<UserVO> page, @Param("ew") Map<String, Object> ew);

    @Select("<script> select CONCAT(id,',',pt_level) from shop_user where 1=1 " +
            "<if test='params != null and params.ptLevel != null'> and pt_level &lt;= #{params.ptLevel} </if>" +
            "<if test='params.userIds != null'> and id in <foreach collection=\"params.userIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> " +
			" </script>")
	List<Object> listUserIdAndLevel(@Param("params") Map<String, Object> params);

    @Select("select CONCAT(id,',',pt_level) from shop_user where id = #{userId} ")
	Object getUserIdAndLevel(@Param("userId") Long userId);

    @Select("select count(1) num from shop_user_invite ui inner join shop_user u on u.id = ui.id where ui.pid = #{uid} and u.pt_eff = #{ptEff}")
    int countToForEverMember(@Param("uid") Long uid, @Param("ptEff") Integer ptEff);



    @Select("<script>SELECT sub.*, so.order_no, so.sys_cnl, so.nickname FROM shop_order so, shop_order_describe sod, shop_user_buyer sub " +
            "WHERE so.order_no = sod.order_no AND sod.user_buyer_id = sub.id " +
            "<if test='params != null and params.userId != null'> and so.user_id =  #{params.userId} </if>"+
            "<if test='params != null and params.cardNo != null'> and sub.card_no like #{params.cardNo} </if>"+
            "<if test='params != null and params.orderNo != null'> and so.order_no like #{params.orderNo} </if>"+
            " ORDER BY sub.add_time DESC limit  #{params.start},#{params.page} </script>")
    List<AdminRealNameDTO> queryBuyerList(@Param("params") Map<String, Object> params);

    @Select("<script>SELECT count(*) FROM shop_order so, shop_order_describe sod, shop_user_buyer sub WHERE so.order_no = sod.order_no AND sod.user_buyer_id = sub.id  " +
            "<if test='params != null and params.userId != null'> and so.user_id =  #{params.userId} </if>"+
            "<if test='params != null and params.cardNo != null'> and sub.card_no like #{params.cardNo} </if>"+
            "<if test='params != null and params.orderNo != null'> and so.order_no like #{params.orderNo} </if> "+
            "</script>")
    Integer countBuyerList(@Param("params") Map<String, Object> params);

    @Select("select demo_cycle(#{id})")
    Object testcc(@Param("id") Long id);

    @Select("<script> SELECT a.id, c.total_buy, c.total_sale, c.total_income, c.total_uava_income " +
            "FROM shop_user a " +
            "LEFT JOIN shop_user_income c ON c.user_id=a.id " +
            "WHERE a.id = #{id} </script>")
    UserVO accountInfo(@Param("id") Integer id);

    @Select("<script> SELECT id from shop_user </script>")
    List<Long> getIdList();

    @Select("SELECT id from shop_user")
    List<User> xxx();

    @Select("<script>SELECT a.id,a.nickname,a.mobile,b.nickname wxnickname,b.img_url,b.bind_type " +
            "FROM shop_user a JOIN shop_user_bind_third b ON b.user_id=a.id " +
            "WHERE b.bind_status=1 " +
            "<if test='params.id != null '> and a.id =  #{params.id} </if>"+
            "<if test='params.mobile != null and params.mobile != \"\"'> and a.mobile like  #{params.mobile} </if>"+
            "<if test='params.nickname != null and params.nickname != \"\"'> and a.nickname like  #{params.nickname} </if>"+
            "<if test='params.wxnickname != null and params.wxnickname != \"\"'> and b.nickname like  #{params.wxnickname} </if>"+
            "<if test='params.bindType != null '> and b.bind_type =  #{params.bindType} </if>"+
            " ORDER BY b.update_time DESC </script>")
    List<WxUserDTO> wxUserList(IPage<WxUserDTO> page, WxUserDTO params);

    /**
     * 根据手机号+区号 或者 授权号获取用户信息
     *
     * @param mobile
     * @param areaCode
     * @param authNo
     * @return
     */
    @Select("<script> " +
            " SELECT u.* FROM shop_user u , shop_user_partner p" +
            " WHERE u.id = p.id AND u.deleted = 0 AND STATUS = 1" +
            "<if test='userId != null and userId != \"\" '> AND u.id = #{userId} </if>"+
            "<if test='mobile != null and mobile != \"\" '> AND u.mobile = #{mobile} </if>"+
            "<if test='areaCode != null and areaCode != \"\" '> AND u.area_code = #{areaCode} </if>"+
            "<if test='authNo != null and authNo != \"\" '> AND p.auth_no = #{authNo} </if>"+
            "</script>")
    User getByIdOrMobileOrAuthNo(@Param("userId") Long userId,
                                 @Param("mobile") String mobile,
                                 @Param("areaCode") String areaCode,
                                 @Param("authNo") String authNo);
}
