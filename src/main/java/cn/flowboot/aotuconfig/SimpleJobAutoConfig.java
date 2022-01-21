package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/20
 */
@Configuration
@ConditionalOnBean(CoordinatorRegistryCenter.class)
@AutoConfigureAfter(ZookeeperAutoConfig.class)
public class SimpleJobAutoConfig extends AbstractJobAutoConfig{


    /**
     * @PostConstruct 在对象加载完依赖注入后执行
     * 初始化 Simple Job
     */
    @PostConstruct
    private void initSimpleJob(){
            initInstance(SimpleJob.class,ElasticSimpleJob.class);
    }


    @Override
    public void initElasticJob(Object instance) {
        ElasticSimpleJob elasticSimpleJob = instance.getClass().getAnnotation(ElasticSimpleJob.class);
        String jobName = elasticSimpleJob.name();
        String cron = elasticSimpleJob.cron();
        int shardingTotalCount = elasticSimpleJob.shardingTotalCount();
        boolean override = elasticSimpleJob.override();
        Class<?> jobStrategy = elasticSimpleJob.jobStrategy();
        boolean isJobEvent = elasticSimpleJob.jobEvent();
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
        //校验参数是否存在
        verificationAttribute(jobName, cron, shardingTotalCount);
        //job 核心配置
        JobCoreConfiguration jcc = JobCoreConfiguration
                .newBuilder(jobName,cron,shardingTotalCount)
                .build();
        //job 类配置
        JobTypeConfiguration jtc = new SimpleJobConfiguration(jcc, instance.getClass().getCanonicalName());

        //job 根的配置 （LiteJobConfiguration）
        LiteJobConfiguration configuration = LiteJobConfiguration
                .newBuilder(jtc)
                .jobShardingStrategyClass(jobStrategy.getCanonicalName())
                //覆盖配置
                .overwrite(override)
                .build();

        if (isJobEvent){
            //配置数据源
            JobEventConfiguration jec = new JobEventRdbConfiguration(getDataSource());
            new SpringJobScheduler((ElasticJob) instance, getZkCenter(), configuration,jec,listenerInstances).init();
        } else {
           // new SpringJobScheduler((ElasticJob) instance, getZkCenter(), configuration,listenerInstances).init();
            new SpringJobScheduler((ElasticJob) instance, getZkCenter(), configuration,listenerInstances).init();
        }
    }


}
