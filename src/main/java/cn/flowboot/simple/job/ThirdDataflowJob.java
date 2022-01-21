package cn.flowboot.simple.job;

import cn.flowboot.aotuconfig.ElasticDataflowJob;
import cn.flowboot.simple.entity.Order;
import cn.flowboot.simple.model.DataflowOrder;
import cn.flowboot.simple.service.OrderService;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
//@ElasticDataflowJob(name = "thirdDataflowJob",cron = "0/30 * * * * ?",shardingTotalCount = 2,override = true,streamingProcess = false)
public class ThirdDataflowJob implements DataflowJob<Order> {

    @Autowired
    private OrderService orderService;

    @Override
    public List<Order> fetchData(ShardingContext shardingContext) {
        //0 天猫 1 京东
        int shardingItem = shardingContext.getShardingItem();
        List<Order> orders = orderService.thirdOrder(shardingItem, 10);
        System.out.printf("当前%d项 抓取%s订单:%d 条\n",shardingItem,(shardingItem == 0?"天猫":"京东"),orders.size());
        return orders;
    }

    @Override
    public void processData(ShardingContext shardingContext, List<Order> orders) {
        int shardingItem = shardingContext.getShardingItem();
        System.out.printf("当前%d项 入库%s订单:%d 条\n",shardingItem,(shardingItem == 0?"天猫":"京东"),orders.size());
        //orderService.saveBatch(orders);
    }
}
