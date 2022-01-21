package cn.flowboot.simple.service;

import cn.flowboot.simple.entity.Order;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author by Vincent Vic
 * @since 2022-01-20
 */
public interface OrderService extends IService<Order> {

    /**
     * 生成订单
     * @param total 生成数量
     * @return
     */
    boolean generatorOrder(int total);


    /**
     * 获取超时订单
     * @param now
     * @param shardingTotalCount
     * @param shardingItem
     * @return
     */
    List<Order> getOrdersByOvertime(Date now, int shardingTotalCount, int shardingItem,int processesNums);

    /**
     * 取消订单
     * @param orders
     * @return
     */
    boolean cancelOrder(List<Order> orders);

    /**
     * 获取多少分订单
     * @param type 0 天猫 1 京东
     * @return
     */
    List<Order> thirdOrder(int type,int nums);
}
