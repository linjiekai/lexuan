package com.zhuanbo.core.dto;

import com.zhuanbo.core.entity.OrderShip;
import lombok.Data;

import java.util.List;

@Data
public class AdminShipsDTO {
    List<OrderShip> list;
}
