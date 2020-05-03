package com.zhuanbo.admin.api.dto.req;

import com.zhuanbo.core.entity.Order;
import com.zhuanbo.core.entity.OrderGoods;
import com.zhuanbo.core.entity.User;
import lombok.Data;

import java.util.List;

/**
 * 后台订单详情DTO
 */
@Data
public class OrderDTO {

    private Order order;
    private User user;
    private List<OrderGoods> orderGoods;


}
