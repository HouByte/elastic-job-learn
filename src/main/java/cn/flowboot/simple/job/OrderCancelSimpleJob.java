package cn.flowboot.simple.job;

import cn.flowboot.aotuconfig.ElasticSimpleJob;
import cn.flowboot.simple.entity.Order;
import cn.flowboot.simple.service.OrderService;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.List;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
//@ElasticSimpleJob(name = "orderCancelSimpleJob",cron = "0/5 * * * * ?",shardingTotalCount = 2,override = true)
public class OrderCancelSimpleJob implements SimpleJob {

    @Autowired
    private OrderService orderService;

    /**
     * 订单取消
     * @param shardingContext
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("===========订单自动取消============== :"+shardingContext.getShardingItem());
        //设置时间30秒前
        Calendar now = Calendar.getInstance();
        now.add(Calendar.SECOND,-30);
        List<Order> orders = orderService.getOrdersByOvertime(now.getTime(), shardingContext.getShardingTotalCount(), shardingContext.getShardingItem(),200);
        if (orders.size() > 0){
            System.out.println("订单自动取消: "+orders.size());
            orderService.cancelOrder(orders);
        } else {
            System.out.println("订单自动取消: 0");
        }
    }
}
