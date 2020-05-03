package com.zhuanbo.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhuanbo.core.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品基本信息表 Mapper 接口
 * </p>
 *
 * @author rome
 * @since 2018-12-19
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    @Select("select id,name from shop_goods where deleted = 0")
    List<Goods> getPartGoods(IPage<Goods> page);

    @Select("select count(1) from shop_goods where deleted = 0")
    Integer getGoodsTotal();

    @Update("update shop_goods set buyer_number = buyer_number + #{number} where id = #{id}")
    int updateBuyerNumber(@Param("id") Integer id, @Param("number") Integer number);


    @Select("<script> " +
            " select g.* from shop_category_relation r, shop_goods g "
            + " where g.deleted=0 and ( "
			+ " <if test='ew.categoryIds != null and ew.categoryIds.size() > 0'> ( r.category_id=g.category_id and r.category_id in <foreach collection=\"ew.categoryIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> )</if> "
			+ " <if test='ew.categoryIds != null and ew.categoryIds.size() > 0 and ew.goodsIds != null and ew.goodsIds.size() > 0 '> or </if> "
			+ " <if test='ew.goodsIds != null and ew.goodsIds.size() > 0'> (r.goods_id=g.id and g.id in <foreach collection=\"ew.goodsIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> )</if> "
            + " )"
			+ " <if test='ew.showIds != null and ew.showIds.size() > 0'> and r.show_id in <foreach collection=\"ew.showIds\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if> "
			+ " <if test='ew.status != null'> and g.status in <foreach collection=\"ew.status\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if>"
			+ "<if test='ew.goodsType != null'> and g.goods_type = #{ew.goodsType} </if>"
			+ "<if test='ew.buyerPartner != null'> and g.buyer_partner = #{ew.buyerPartner} </if>"
            + "order by r.id desc, g.id desc"
			+ "</script>")
    List<Goods> pageCustom(Page<Goods> page, @Param("ew") Map<String, Object> ew);

    @Select("<script> " +
            " select g.* from shop_goods g "
            + " where g.deleted=0  "
            + " <if test='ew.brandId != null'> and g.brand_id = #{ew.brandId} </if>"
            + " <if test='ew.status != null'> and g.status in <foreach collection=\"ew.status\" index=\"index\" item=\"item\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach> </if>"
			+ "<if test='ew.goodsType != null'> and g.goods_type = #{ew.goodsType} </if>"
			+ "<choose><when test='ew.sales_num !=null'> order by g.sales_num desc</when>"
			+"<when test='ew.base_score !=null'> order by g.base_score desc</when>"
			+"<when test='ew.priceAsc !=null'> order by g.price </when>"
			+"<when test='ew.priceAsc !=null'> order by g.price desc</when>"
            + "<otherwise>order by g.id desc</otherwise></choose>"
			+ "<if test='ew.buyerPartner != null'> and g.buyer_partner = #{ew.buyerPartner} </if>"
            //+ "order by g.id desc"
			+ "</script>")
	List<Goods> page(Page<Goods> page, @Param("ew") Map<String, Object> ew);

}
