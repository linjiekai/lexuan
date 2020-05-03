package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhuanbo.core.dto.AdminPointDTO;
import com.zhuanbo.core.dto.AdminUserIncomeDTO;
import com.zhuanbo.core.entity.UserIncome;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;


/**
 * <p>
 * 用户收益表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2019-06-13
 */
public interface UserIncomeMapper extends BaseMapper<UserIncome> {

	/**
     * 增加商品订单累计收益和在途收益
     * @param id
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income + #{price}, total_uava_income = total_uava_income + #{price}, share_income = share_income + #{price} where user_id = #{userId}")
	int addTotalAndUavaAndShare(@Param("userId") Long userId, @Param("price") BigDecimal price);
    
	/**
     * 扣减商品订单累计收益和在途收益
     * @param userId
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income - #{price}, total_uava_income = total_uava_income - #{price}, share_income = share_income - #{price} where user_id = #{userId}")
	int subtractTotalAndUavaAndShare(@Param("userId")Long userId, @Param("price")BigDecimal price);
    
    /**
     * 增加充值订单累计收益和在途收益
     * @param id
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income + #{price}, total_uava_income = total_uava_income + #{price}, train_income = train_income + #{price} where user_id = #{userId}")
	int addTotalAndUavaAndTrain(@Param("userId") Long userId, @Param("price") BigDecimal price);
    
	/**
     * 扣减充值订单累计收益和在途收益
     * @param userId
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income - #{price}, total_uava_income = total_uava_income - #{price}, train_income = train_income - #{price} where user_id = #{userId}")
	int subtractTotalAndUavaAndTrain(@Param("userId")Long userId, @Param("price")BigDecimal price);
    
    /**
     * 增加累计收益
     * @param userId
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income + #{price} where user_id = #{userId}")
	int addTotalIncome(@Param("userId")Long userId, @Param("price")BigDecimal price);
    
    /**
     * 扣减累计收益
     * @param id
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income - #{price} where user_id = #{userId}")
	int subtractTotalIncome(@Param("userId")Long userId, @Param("price")BigDecimal price);
    
    /**
     * 增加在途收益
     * @param id
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_uava_income = total_uava_income + #{price} where user_id = #{userId}")
	int addUavaIncome(@Param("userId")Long userId, @Param("price")BigDecimal price);
    
	/**
     * 扣减在途收益
     * @param id
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_uava_income = total_uava_income - #{price} where user_id = #{userId}")
	int subtractUavaIncome(@Param("userId")Long userId, @Param("price")BigDecimal price);

    /**
     * 订单完成后，在途收益和分享、自买更变
     * @param userId 用户id
     * @param totalPrice 在途中收益
     * @param selfPrice 自省收益
     * @param sharePrice 分享收益
     * @return
     */
    @Update("update shop_user_income set total_uava_income = total_uava_income - #{totalPrice},share_uava_income = share_uava_income - #{sharePrice},econ_uava_income = econ_uava_income - #{selfPrice} where user_id = #{userId}")
    int updateByFinishOrder(@Param("userId") Long userId, @Param("totalPrice") BigDecimal totalPrice, @Param("selfPrice") BigDecimal selfPrice, @Param("sharePrice") BigDecimal sharePrice);

    /**
     * 增加累计收益和在途收益
     * @param id
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income + #{price}, total_uava_income = total_uava_income + #{price} where user_id = #{userId}")
    int addTotalAndUavaIncome(@Param("userId") Long userId, @Param("price") BigDecimal price);

    /**
     * 扣减累计收益和在途收益
     * @param userId
     * @param price
     * @return
     */
    @Update("update shop_user_income set total_income = total_income - #{price}, total_uava_income = total_uava_income - #{price} where user_id = #{userId}")
    int subtractTotalAndUavaIncome(@Param("userId")Long userId, @Param("price")BigDecimal price);

    /**
     * 增加总积分和可用积分
     * @param userId
     * @param point
     * @return
     */
    @Update("update shop_user_income set total_point = total_point + #{point}, usable_point = usable_point + #{point} where user_id = #{userId}")
    int addTotalAndUsablePoint(@Param("userId")Long userId, @Param("point")Integer point);

    /**
     * 扣减可用积分
     * @param userId
     * @param point
     * @return
     */
    @Update("update shop_user_income set usable_point = usable_point - #{point}, use_point = use_point + #{point} where user_id = #{userId}")
    int subtractUsablePoint(@Param("userId")Long userId, @Param("point")Integer point);

    /**
     * 获取积分信息
     *
     * @return
     */
    @Select("<script>" +
            " SELECT i.user_id, u.nickname,u.pt_level,u.mobile,i.usable_point FROM shop_user_income i, shop_user u " +
            " WHERE i.user_id = u.id AND u.deleted = 0 AND u.status = 1 AND u.pt_level = 5 " +
            " <if test='adminPointDTO.mobile != null and adminPointDTO.mobile != \"\"'> AND u.mobile = #{adminPointDTO.mobile} </if> " +
            " <if test='adminPointDTO.nickname != null and adminPointDTO.nickname != \"\"'> AND u.nickname = #{adminPointDTO.nickname} </if> " +
            " <if test='adminPointDTO.userId != null and adminPointDTO.userId != \"\"'> AND u.id = #{adminPointDTO.userId} </if> " +
            " ORDER BY i.update_time DESC " +
            "</script>")
    List<AdminUserIncomeDTO> pagePointInfo(IPage page, @Param("adminPointDTO") AdminPointDTO adminPointDTO);
}
