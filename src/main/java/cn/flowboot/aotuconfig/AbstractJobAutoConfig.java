package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.api.ElasticJob;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.lite.spring.api.SpringJobScheduler;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/20
 */
@Getter
public abstract class AbstractJobAutoConfig {

    /**
     * 获取Spring上下文
     */
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CoordinatorRegistryCenter zkCenter;

    @Autowired
    private DataSource dataSource;

    /**
     * @PostConstruct 在对象加载完依赖注入后执行
     * 初始化 Simple Job
     */
    public void initInstance(Class<?> jobClass,Class<? extends Annotation> jobAnnotationClass){

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(jobAnnotationClass);

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?>[] interfaces = instance.getClass().getInterfaces();
            for (Class<?> superInterface : interfaces) {
                //判断是否是实例
                if (superInterface == jobClass){
                    initElasticJob(instance);

                }
            }
        }

    }

    public abstract void initElasticJob(Object instance) ;

    public void verificationAttribute(String jobName, String cron, int shardingTotalCount) {
        if (StringUtils.isBlank(jobName)){
            throw new RuntimeException("ElasticSimpleJob:The attribute of name cannot be empty ");
        }
        if (StringUtils.isBlank(cron)){
            throw new RuntimeException("ElasticSimpleJob:The attribute of cron cannot be empty ");
        }
        if (shardingTotalCount <= 0){
            throw new RuntimeException("The attribute of shardingTotalCount cannot be less than or equal to 0 ");
        }
    }
}
