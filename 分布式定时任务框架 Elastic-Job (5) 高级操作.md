@[TOC](分布式定时任务框架 Elastic-Job 五 高级操作)
> 注：跟上文比较紧密
# 分片策略
## 什么是分片策略
- 作业名的哈希值奇偶数决定IP升降序算法的分片策略
- 当有三台服务器时，分片总数为2时
	- 作业名称hash值为奇数时，1=[0],2=[1],3=[] （从前往后分配）
	- 作业名称hash值为偶数时，3=[0],2=[1],1=[] （从后往前分配）

## 自定义分片策略
轮询策略：cn.flowboot.aotuconfig.sharding.PollingShardingStrategy
```java
package cn.flowboot.aotuconfig.sharding;

import com.dangdang.ddframe.job.lite.api.strategy.JobInstance;
import com.dangdang.ddframe.job.lite.api.strategy.JobShardingStrategy;

import java.util.*;

/**
 * <h1>自定义策略：轮询</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/21
 */
public class PollingShardingStrategy implements JobShardingStrategy {
    @Override
    public Map<JobInstance, List<Integer>> sharding(List<JobInstance> jobInstances, String jobName, int shardingTotalCount) {
        Map<JobInstance, List<Integer>> jobInstanceListMap = new HashMap<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>(shardingTotalCount);
        for (int i = 0; i < shardingTotalCount; i++) {
            queue.add(i);
        }
        while (queue.size()>0){
            for (JobInstance jobInstance : jobInstances) {
                if (queue.size() > 0){
                    Integer item = queue.pop();
                    List<Integer> items = jobInstanceListMap.get(jobInstance);
                    if (items != null && items.size() > 0){
                        items.add(item);
                    } else {
                        jobInstanceListMap.put(jobInstance,Collections.singletonList(item));
                    }
                }
            }
        }
        return jobInstanceListMap;
    }
}

```
在@ElasticSimpleJob和@ElasticDataflowJob注解加入分片策略参数

```java
/**
     * 分片策略
     * @return
     */
    Class<?> jobStrategy() default AverageAllocationJobShardingStrategy.class;
```
在SimpleJobAutoConfig和DataflowJobAutoConfig中获取分片策略并设置

```java
Class<?> jobStrategy = elasticSimpleJob.jobStrategy();
...
//job 根的配置 （LiteJobConfiguration）
return LiteJobConfiguration
        ...
        .jobShardingStrategyClass(jobStrategy.getCanonicalName())
        ...

```

# 事件追踪
## 添加数据源
在SimpleJobAutoConfig和DataflowJobAutoConfig中添加
```java
@Autowired
private DataSource dataSource;
...
//配置数据源
JobEventConfiguration jec = new JobEventRdbConfiguration(getDataSource());
new SpringJobScheduler((ElasticJob) instance, getZkCenter(), configuration,jec).init();
```
依旧可以在注解添加属性决定是否开启添加数据源，本步骤略
运行后（数据源需要配置且存在）数据库将会多出两个表：
```sql
job_execution_log
job_status_trace_log
```

# 作业监听器
编写监听器
```java
package cn.flowboot.simple.listner;

import com.dangdang.ddframe.job.executor.ShardingContexts;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/21
 */
public class MyNormalListener implements ElasticJobListener {
    @Override
    public void beforeJobExecuted(ShardingContexts shardingContexts) {
        System.out.println("作业["+shardingContexts.getJobName()+"] 方法前");
    }

    @Override
    public void afterJobExecuted(ShardingContexts shardingContexts) {
        System.out.println("作业["+shardingContexts.getJobName()+"] 方法后");
    }
}

```
自动化配置添加代码块，在initElasticJob方法中修改

```java
Class<? extends ElasticJobListener>[] listeners = elasticSimpleJob.jobListener();
ElasticJobListener[] listenerInstances = null;
if (listeners != null && listeners.length > 0){
    listenerInstances = new ElasticJobListener[listeners.length];
    int i = 0;
    for (Class<? extends ElasticJobListener> listener : listeners) {
        try {
            listenerInstances[i++] = listener.getDeclaredConstructor().newInstance();

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
} else {
    listenerInstances = new ElasticJobListener[0];
}
new SpringJobScheduler((ElasticJob) instance, getZkCenter(), configuration,listenerInstances).init();
```
 
