package cn.flowboot.simple.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author by Vincent Vic
 * @since 2022-01-20
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("`order`")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单金额
     */
    private BigDecimal price;

    /**
     * 订单状态0 未支付 1支付，2 已支付
     */
    private Integer status;

    /**
     * 收货人
     */
    private String receiveName;

    /**
     * 收货电话
     */
    private String receivePhone;

    /**
     * 收货地址
     */
    private String receiveAddress;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 更新人
     */
    private String updateUser;

    private Date createTime;

    private Date updateTime;


}
