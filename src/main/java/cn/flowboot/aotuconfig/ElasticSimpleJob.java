package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.api.strategy.impl.AverageAllocationJobShardingStrategy;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/20
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ElasticSimpleJob {

    /**
     * 任务名称
     */
    String name() default  "";
    /**
     * cron表达式，用于控制作业触发时间,默认每间隔10秒钟执行一次
     */
    String cron() default  "";
    /**
     * 作业分片总数
     */
    int shardingTotalCount() default 1;

    /**
     * 是否可覆盖
     */
    boolean override() default false;

    /**
     * 分片策略
     */
    Class<?> jobStrategy() default AverageAllocationJobShardingStrategy.class;

    /**
     * 是否支持事件记录
     */
    boolean jobEvent() default false;

    /**
     * 监听器
     * @return
     */
    Class<? extends ElasticJobListener>[] jobListener() default {};

}
