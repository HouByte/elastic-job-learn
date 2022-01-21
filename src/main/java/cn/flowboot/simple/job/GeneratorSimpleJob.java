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
//@ElasticSimpleJob(name = "generatorSimpleJob",cron = "0/30 * * * * ?",shardingTotalCount = 1,override = true)
public class GeneratorSimpleJob implements SimpleJob {

    @Autowired
    private OrderService orderService;

    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("生成十条订单");
        orderService.generatorOrder(10);
    }
}
