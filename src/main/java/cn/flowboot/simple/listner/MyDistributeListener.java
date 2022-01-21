package cn.flowboot.simple.listner;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.AbstractDistributeOnceElasticJobListener;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/21
 */
public class MyDistributeListener extends AbstractDistributeOnceElasticJobListener {
    public MyDistributeListener(long startedTimeoutMilliseconds, long completedTimeoutMilliseconds) {
        super(startedTimeoutMilliseconds, completedTimeoutMilliseconds);
    }

    @Override
    public void doBeforeJobExecutedAtLastStarted(ShardingContexts shardingContexts) {
        System.out.println("分布式监听器 - 作业["+shardingContexts.getJobName()+"] 方法前");

    }

    @Override
    public void doAfterJobExecutedAtLastCompleted(ShardingContexts shardingContexts) {
        System.out.println("分布式监听器 - 作业["+shardingContexts.getJobName()+"] 方法后");

    }
}
