@[TOC](分布式定时任务框架 Elastic-Job 三 Simple作业实战【订单自动取消】)
# 应用场景
简述：在电商及相关场景下，订单创建超过n分钟/小时未支付的进行自动取消
# 场景搭建
## 数据库创建
创建订单表
```sql
CREATE TABLE `order`  (
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `price` decimal(10, 2) NOT NULL COMMENT '订单金额',
  `status` int(5) NOT NULL DEFAULT 0 COMMENT '订单状态0 未支付 1已支付，2 已取消',
  `receive_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收货人',
  `receive_phone` varchar(12) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收货电话',
  `receive_address` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '收货地址',
  `create_user` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_user` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

```

## Spring 集成持久化
本文用的是Mybatis框架中的 Plus版本，自行集成，可参考如下文章，任意一个即可，非重点 
[Spring Boot 集成Mybatis](https://blog.csdn.net/Vincent_Vic_/article/details/122491021)
[Spring Boot 集成Mybatis Plus](https://blog.csdn.net/Vincent_Vic_/article/details/122499604)
[Spring Boot 集成TK Mybatis](https://blog.csdn.net/Vincent_Vic_/article/details/122506520)

## 服务开发
OrderServiceImpl 其他类略，功能仅演示定时任务
```java
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
}

```
由于测试，需要生成订单，也通过定时任务来生成

```java
package cn.flowboot.simple.job;

import cn.flowboot.aotuconfig.ElasticSimpleJob;
import cn.flowboot.simple.service.OrderService;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
@ElasticSimpleJob(name = "generatorSimpleJob",cron = "0/30 * * * * ?",shardingTotalCount = 1,override = true)
public class GeneratorSimpleJob implements SimpleJob {

    @Autowired
    private OrderService orderService;

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("生成十条订单");
        orderService.generatorOrder(10);
    }
}

```
自动取消任务编写，获取过期的订单，根据设定业务规则分配机器分摊操作订单，本业务通过去摸分摊任务，可以启动两台实例进行模拟，如果只有一台，也会通过一台机器完成定时任务

```java
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
@ElasticSimpleJob(name = "orderCancelSimpleJob",cron = "0/5 * * * * ?",shardingTotalCount = 2,override = true)
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

```


