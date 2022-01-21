package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Map;

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
public  class DataflowJobAutoConfig  extends AbstractJobAutoConfig{


    /**
     * @PostConstruct 在对象加载完依赖注入后执行
     * 初始化 Simple Job
     */
    @PostConstruct
    private void initSimpleJob(){
        initInstance(DataflowJob.class,ElasticDataflowJob.class);
    }


    @Override
    public void initElasticJob(Object instance) {
        ElasticDataflowJob elasticDataflowJob = instance.getClass().getAnnotation(ElasticDataflowJob.class);
        String jobName = elasticDataflowJob.name();
        String cron = elasticDataflowJob.cron();
        int shardingTotalCount = elasticDataflowJob.shardingTotalCount();
        boolean override = elasticDataflowJob.override();
        boolean streamingProcess = elasticDataflowJob.streamingProcess();
        Class<?> jobStrategy = elasticDataflowJob.jobStrategy();
        boolean isJobEvent = elasticDataflowJob.jobEvent();
        //校验参数是否存在
        verificationAttribute(jobName, cron, shardingTotalCount);
        //job 核心配置
        JobCoreConfiguration jcc = JobCoreConfiguration
                .newBuilder(jobName,cron,shardingTotalCount)
                .build();
        //job 类配置
        JobTypeConfiguration jtc = new DataflowJobConfiguration(jcc, instance.getClass().getCanonicalName(),streamingProcess);

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
            new SpringJobScheduler((ElasticJob) instance, getZkCenter(), configuration,jec).init();
        } else {
            new SpringJobScheduler((ElasticJob) instance, getZkCenter(), configuration).init();
        }
    }


}
