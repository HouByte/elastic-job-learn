package cn.flowboot.simple.sharding;

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
