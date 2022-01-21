package cn.flowboot.simple.job;

import cn.flowboot.aotuconfig.ElasticSimpleJob;
import cn.flowboot.simple.listner.MyNormalListener;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;

import java.time.LocalTime;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
@ElasticSimpleJob(name = "mySimpleJob",cron = "0/10 * * * * ?",shardingTotalCount = 2,override = true,jobEvent = false,jobListener = MyNormalListener.class)
public class MySimpleJob implements SimpleJob {


    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.printf("SimpleJob %s 当前分片项 %d,总分片项 %d\n", LocalTime.now(),shardingContext.getShardingItem(),shardingContext.getShardingTotalCount());
    }
}
