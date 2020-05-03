package com.zhuanbo.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "level.upgrade")
@Data
public class LevelUpGradeConfig {

    private Integer d2tADaNumberx;// 达人->体验官：a方案：团队n个达人
    private Integer d2tADirectDaNumberx;// 达人->体验官：a方案：n个直属达人
    private BigDecimal d2tAPricex;// 达人->体验官：a方案：累积业绩

    private Integer d2tBDirectDaNumberx;// 达人->体验官：b方案：n个直属达人
    private BigDecimal d2tBPricex;// 达人->体验官：b方案：累积业绩

    private Integer t2sTiNumbexr;// 体验官->司令：团队n个体验官
    private Integer t2sDirectTiNumberx;// 体验官->司令：团队n个直属体验官
    private Integer t2sDaNumberx;// // 体验官->司令：团队n个达人
    private BigDecimal t2sPricex;// // 体验官->司令：业绩
}
