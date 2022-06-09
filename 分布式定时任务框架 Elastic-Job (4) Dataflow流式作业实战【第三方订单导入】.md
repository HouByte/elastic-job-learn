@[TOC](分布式定时任务框架 Elastic-Job 三 Dataflow流式作业实战【第三方订单导入】)
# 应用场景
电商公司在天猫，京东等多个平台运营，需要统计各平台的交易数据，所以定时将各平台订单汇总至运营系统，以便运营
# 模拟第三方订单
> 在前文基础上添加
模拟第三方订单，这里仅作为demo测试与实际业务无关
```java
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
```

# 编写定时任务
两个分片，一个处理天猫，一个处理京东，streamingProcess 流式处理关闭时和simple作业类似定时处理，一个负责抓取，一个负责处理，流式处理开启时，在抓取和处理循环操作，直到数据抓取完毕或者重新分片
```java
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
@ElasticDataflowJob(name = "thirdDataflowJob",cron = "0/30 * * * * ?",shardingTotalCount = 2,override = true,streamingProcess = false)
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
        orderService.saveBatch(orders);
    }
}

```

