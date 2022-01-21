package cn.flowboot.simple.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
@Data
public class DataflowOrder {

    private Integer orderId;
    //0 处理 1未处理
    private Integer status;

}
