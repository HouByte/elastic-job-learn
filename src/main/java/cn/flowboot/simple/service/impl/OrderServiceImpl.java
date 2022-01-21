package cn.flowboot.simple.service.impl;

import cn.flowboot.simple.entity.Order;
import cn.flowboot.simple.mapper.OrderMapper;
import cn.flowboot.simple.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author by Vincent Vic
 * @since 2022-01-20
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    /**
     * 生成订单
     *
     * @param total 生成数量
     * @return
     */
    @Override
    public boolean generatorOrder(int total) {
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            Random random = new Random();
            Order order = Order
                    .builder()
                    .price(new BigDecimal(random.nextInt(4000)+1000))
                    .createUser("订单生成器")
                    .updateUser("订单生成器")
                    .receiveName("张三"+System.currentTimeMillis())
                    .receiveAddress("福建省厦门市集美区XXXX"+System.currentTimeMillis())
                    .receivePhone("1889988"+(random.nextInt(4000)+1000))
                    .createTime(new Date())
                    .updateTime(new Date())
                    .build();
            orders.add(order);
        }
        return saveBatch(orders);
    }

    /**
     * 获取超时订单
     *
     * @param now
     * @param shardingTotalCount
     * @param shardingItem
     * @return
     */
    @Override
    public List<Order> getOrdersByOvertime(Date now, int shardingTotalCount, int shardingItem,int processesNums) {
        //订单尾号 % 分片总数 == 当前分片项
        //创建时间超过指定时间
        //订单未支付
        //一次处理 processesNums条
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status",0).lt("create_time",now)
                .last(" and id % "+ shardingTotalCount + " = " + shardingItem + " ORDER BY create_time ASC  limit "+processesNums);
        return list(queryWrapper);
    }

    /**
     * 取消订单
     *
     * @param orders
     * @return
     */
    @Override
    public boolean cancelOrder(List<Order> orders) {
        orders.forEach(o -> o.setStatus(2));
        return updateBatchById(orders);
    }

    /**
     * 获取多少分订单
     *
     * @param type 0 天猫 1 京东
     * @param total
     * @return
     */
    @Override
    public List<Order> thirdOrder(int type, int total) {
        String typeStr = type == 0 ? "天猫":"京东";
        List<Order> orders = new ArrayList<>();
        //一天前
        Calendar last = Calendar.getInstance();
        last.add(Calendar.HOUR,-24);
        for (int i = 0; i < total; i++) {
            Random random = new Random();
            Order order = Order
                    .builder()
                    .price(new BigDecimal(random.nextInt(6000)+1000))
                    .createUser(typeStr+"订单生成器")
                    .updateUser(typeStr+"订单生成器")
                    .receiveName(typeStr+"张三"+System.currentTimeMillis())
                    .receiveAddress("福建省厦门市集美区"+typeStr+System.currentTimeMillis())
                    .receivePhone("1889988"+(random.nextInt(5000)+1000))
                    .createTime(last.getTime())
                    .updateTime(last.getTime())
                    .build();
            orders.add(order);
        }
        return orders;
    }
}
