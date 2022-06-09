@[TOC](分布式定时任务框架Elastic-Job 二 Spring / Spring Boot 整合)

# 一、Spring 集成
## 1.1 创建Spring项目
IDEA为例：文件 -> 新建项目 -> Maven -> 原型中创建 -> org.apache.maven.archetypes:maven-archetype-webapp

## 1.2 添加依赖
添加spring 依赖
```xml
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-context</artifactId>
  <version>5.3.14</version>
</dependency>
<dependency>
  <groupId>org.springframework</groupId>
  <artifactId>spring-web</artifactId>
  <version>5.3.14</version>
</dependency>
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.18.22</version>
</dependency>
<dependency>
  <groupId>org.slf4j</groupId>
  <artifactId>slf4j-log4j12</artifactId>
  <version>1.7.32</version>
</dependency>
```
## 1.3 添加Elastic-Job依赖
```xml
<dependency>
  <groupId>com.dangdang</groupId>
  <artifactId>elastic-job-lite-core</artifactId>
  <version>2.1.5</version>
</dependency>
<dependency>
  <groupId>com.dangdang</groupId>
  <artifactId>elastic-job-lite-spring</artifactId>
  <version>2.1.5</version>
</dependency>
```

## 1.4 配置
配置web.xml
```xml
<!DOCTYPE web-app PUBLIC
 "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
 "http://java.sun.com/dtd/web-app_2_3.dtd" >

<web-app>
  <display-name>Spring elastic-job</display-name>
  <context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath*:spring-config.xml</param-value>
  </context-param>
  
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
</web-app>

```
配置spring-config.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:reg="http://www.dangdang.com/schema/ddframe/reg"
       xmlns:job="http://www.dangdang.com/schema/ddframe/job"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.dangdang.com/schema/ddframe/reg
       http://www.dangdang.com/schema/ddframe/reg/reg.xsd
       http://www.dangdang.com/schema/ddframe/job
       http://www.dangdang.com/schema/ddframe/job/job.xsd
">
    <!--注册中心配置-->
    <reg:zookeeper server-lists="localhost:2181" namespace="spring-elasticjob" id="zkCenter"/>
    <!-- simple作业配置 -->
    <job:simple registry-center-ref="zkCenter" cron="0/10 * * * * ?" sharding-total-count="2" class="cn.flowboot.job.MySimpleJob"/>
</beans>


</beans>

```
> 其中在xsi:schemaLocation中添加schema,配置注册中心和 simple作业配置
MySimpleJob

```java
package cn.flowboot.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;


/**
 * <h1>My Simple Job</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/19
 */
@Slf4j
public class MySimpleJob implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        log.info("SimpleJob %s 当前分片项 %d,总分片项 %d\n", LocalTime.now(),shardingContext.getShardingItem(),shardingContext.getShardingTotalCount());

    }
}
```

# Spring Boot 集成
## 创建Spring Boot项目
IDEA为例：文件 -> 新建项目 -> Spring Initializr
## 添加依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
</dependency>
<!--配置-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
</dependency>
<!-- elastic-job-lite-->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>2.1.5</version>
</dependency>
<dependency>
   <groupId>com.dangdang</groupId>
   <artifactId>elastic-job-lite-spring</artifactId>
   <version>2.1.5</version>
</dependency>
<!-- 用于测试 -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13</version>
    <scope>test</scope>
</dependency>
<!-- 用于日志和实体 -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.22</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.32</version>
</dependency>
```

## 属性类
ZookeeperProperties.java
```java
package cn.flowboot.simple.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/19
 */
@Data
@ConfigurationProperties(prefix = "elastic-job.zookeeper")
public class ZookeeperProperties {

    /**
     * zookeeper服务器地址
     */
    private String server = "localhost:2181";
    /**
     * zookeeper命名空间
     */
    private String namespace = "elastic-job-zookeeper";
    /**
     * 等待重试的间隔时间的初始值 默认1000，单位：毫秒
     */
    private Integer baseSleepTimeMilliseconds = 1000;
    /**
     * 等待重试的间隔时间的最大值 默认3000，单位：毫秒
     */
    private Integer maxSleepTimeMilliseconds = 3000;
    /**
     * 最大重试次数 默认3
     */
    private Integer maxRetries = 3;
    /**
     * 会话超时时间 默认60000，单位：毫秒
     */
    private Integer sessionTimeoutMilliseconds = 60000;
    /**
     * 连接超时时间 默认15000，单位：毫秒
     */
    private Integer  connectionTimeoutMilliseconds = 15000;
}

```

## 自动配置
### Zookeeper自动配置
ZookeeperAutoConfig.java
```java
package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/20
 */
@Configuration
@ConditionalOnProperty("elastic-job.zookeeper.server")
@EnableConfigurationProperties(ZookeeperProperties.class)
public class ZookeeperAutoConfig {

    @Autowired
    private ZookeeperProperties zookeeperProperties;
    @Bean(initMethod = "init")
    public CoordinatorRegistryCenter zkCenter(){
        ZookeeperConfiguration zc = new ZookeeperConfiguration(zookeeperProperties.getServer(),zookeeperProperties.getNamespace());
        zc.setMaxSleepTimeMilliseconds(zookeeperProperties.getMaxSleepTimeMilliseconds());
        zc.setBaseSleepTimeMilliseconds(zookeeperProperties.getBaseSleepTimeMilliseconds());
        zc.setConnectionTimeoutMilliseconds(zookeeperProperties.getConnectionTimeoutMilliseconds());
        zc.setSessionTimeoutMilliseconds(zookeeperProperties.getSessionTimeoutMilliseconds());
        zc.setMaxRetries(zookeeperProperties.getMaxRetries());
        //初始化 交给Spring执行
        //crc.init();
        return new ZookeeperRegistryCenter(zc);
    }
}


```
#### 修改为可以作为外部jar
根据Spring Boot 文档中 [49.Creating Your Own Auto-configuration](https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/#boot-features-developing-auto-configuration)
![49.Creating Your Own Auto-configuration](https://img-blog.csdnimg.cn/62256c09d768444eab201f40bc73a12f.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAVmluY2VudCBWaWM=,size_20,color_FFFFFF,t_70,g_se,x_16)
来自Edge网页翻译插件
![49.自动配置](https://img-blog.csdnimg.cn/0334eca80cda4f818728589eca6c5c85.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBAVmluY2VudCBWaWM=,size_20,color_FFFFFF,t_70,g_se,x_16)
根据49.2对项目进行改造

将自动配置文件移动到启动类以外的包，涉及类如下
```java
//原包路径cn.flowboot.simple.config下
cn.flowboot.aotuconfig.ZookeeperProperties
cn.flowboot.aotuconfig.ZookeeperAutoConfig
cn.flowboot.aotuconfig.ElasticJobProperties
```
在资源目录下创建META-INF/spring.factories
```xml
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
cn.flowboot.aotuconfig.ZookeeperAutoConfig

```

###  Elastic-Job 自动配置
编写注解，注意这个编写在启动类之外包中，和zookeeper同一个位置即可
#### SimpleJob 配置
```java
package cn.flowboot.aotuconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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

}

```

编写MySimpleJob 
```java
package cn.flowboot.simple.job;

import cn.flowboot.aotuconfig.ElasticSimpleJob;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
@ElasticSimpleJob(name = "mySimpleJob",cron = "0/10 * * * * ?",shardingTotalCount = 2,override = true)
public class MySimpleJob implements SimpleJob {


    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.printf("SimpleJob %s 当前分片项 %d,总分片项 %d\n", LocalTime.now(),shardingContext.getShardingItem(),shardingContext.getShardingTotalCount());
    }
}

```
SimpleJobAutoConfig.java
```java
package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
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
public class SimpleJobAutoConfig {

    /**
     * 获取Spring上下文
     */
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private CoordinatorRegistryCenter zkCenter;

    /**
     * @PostConstruct 在对象加载完依赖注入后执行
     * 初始化 Simple Job
     */
    @PostConstruct
    private void initSimpleJob(){

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ElasticSimpleJob.class);

        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object instance = entry.getValue();
            Class<?>[] interfaces = instance.getClass().getInterfaces();
            for (Class<?> superInterface : interfaces) {
                //判断是否是实例
                if (superInterface == SimpleJob.class){
                    LiteJobConfiguration configuration = initElasticSimpleJob(instance);
                    new SpringJobScheduler((ElasticJob) instance,zkCenter,configuration).init();
                }
            }
        }

    }

    private LiteJobConfiguration initElasticSimpleJob(Object instance) {
        ElasticSimpleJob elasticSimpleJob = instance.getClass().getAnnotation(ElasticSimpleJob.class);
        String jobName = elasticSimpleJob.name();
        String cron = elasticSimpleJob.cron();
        int shardingTotalCount = elasticSimpleJob.shardingTotalCount();
        boolean override = elasticSimpleJob.override();
        //校验参数是否存在
        verificationAttribute(jobName, cron, shardingTotalCount);
        //job 核心配置
        JobCoreConfiguration jcc = JobCoreConfiguration
                .newBuilder(jobName,cron,shardingTotalCount)
                .build();
        //job 类配置
        JobTypeConfiguration jtc = new SimpleJobConfiguration(jcc, instance.getClass().getCanonicalName());

        //job 根的配置 （LiteJobConfiguration）
        return LiteJobConfiguration
                .newBuilder(jtc)
                //覆盖配置
                .overwrite(override)
                .build();
    }

    private void verificationAttribute(String jobName, String cron, int shardingTotalCount) {
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

```
配置添加
META-INF/spring.factories
```java
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
cn.flowboot.aotuconfig.ZookeeperAutoConfig,\
cn.flowboot.aotuconfig.SimpleJobAutoConfig

```

#### Dataflow配置
创建@ElasticDataflowJob 注解 
```java
package cn.flowboot.aotuconfig;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/20
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ElasticSimpleJob
public @interface ElasticDataflowJob {

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
     * 是否流式处理
     */
    boolean streamingProcess() default false;

}

```
使用

```java
package cn.flowboot.simple.job;

import cn.flowboot.aotuconfig.ElasticDataflowJob;
import cn.flowboot.simple.model.Order;
import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.dataflow.DataflowJob;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1></h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
@ElasticDataflowJob(name = "myDataflowJob",cron = "0/10 * * * * ?",shardingTotalCount = 2,override = true,streamingProcess = true)
public class MyDataflowJob implements DataflowJob<Order> {

    private List<Order> orders = new ArrayList<>();

    /**
     * 模拟订单数据
     */
    {
        for (int i = 0; i < 100; i++) {
            Order order = new Order();
            order.setOrderId(i+1);
            order.setStatus(0);
            orders.add(order);
        }
    }

    @Override
    public List<Order> fetchData(ShardingContext shardingContext) {
        //订单号 % 分片总数 == 当前分片项
        List<Order> orderList = orders.stream().filter(o -> o.getStatus() == 0)
                .filter(o -> o.getOrderId() % shardingContext.getShardingTotalCount() == shardingContext.getShardingItem())
                .collect(Collectors.toList());

        //只处理前10个
        if (orderList.size() >0){
            orderList = orderList.subList(0,10);
        }
        //模拟耗时
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("DataflowJob %s 当前分片项 %d,抓取数据是 %s\n", LocalTime.now(),shardingContext.getShardingItem(),orderList);

        return orderList;
    }

    @Override
    public void processData(ShardingContext shardingContext, List<Order> list) {
        System.out.printf("DataflowJob %s 当前分片项 %d 正在处理数据...\n", LocalTime.now(),shardingContext.getShardingItem());
        list.forEach(o->o.setStatus(1));
        System.out.println(list+"\n"+orders);
        //模拟耗时
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

```

##### 简单方法
与SimpleJobAutoConfig 基本一致，核心JobTypeConfiguration 实例为DataflowJobConfiguration，其中第三个参数streamingProcess在注解中定义，可克隆SimpleJobAutoConfig @ElasticSimpleJob 修改为@ElasticDataflowJob 

```java
//job 类配置
JobTypeConfiguration jtc = new DataflowJobConfiguration(jcc, MyDataflowJob.class.getCanonicalName(),streamingProcess);
```

##### 重构
减少代码冗余，两者相似简单抽出公共部分，其中verificationAttribute为公共工具，initInstance为启动入口，传递类型和注解类型，需要实现initElasticJob，不同类型不同实现方法，做个简单的父类，简化过于冗余的代码

```java
package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
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

```
重构SimpleJobAutoConfig 
```java
package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
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
                //覆盖配置
                .overwrite(override)
                .build();
         new SpringJobScheduler((ElasticJob) instance,getZkCenter(),configuration).init();
    }


}

```
DataflowJobAutoConfig  编写
```java
package cn.flowboot.aotuconfig;

import com.dangdang.ddframe.job.api.simple.SimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
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
    public LiteJobConfiguration initElasticJob(Object instance) {
        ElasticDataflowJob elasticDataflowJob = instance.getClass().getAnnotation(ElasticDataflowJob.class);
        String jobName = elasticDataflowJob.name();
        String cron = elasticDataflowJob.cron();
        int shardingTotalCount = elasticDataflowJob.shardingTotalCount();
        boolean override = elasticDataflowJob.override();
        boolean streamingProcess = elasticDataflowJob.streamingProcess();
        //校验参数是否存在
        verificationAttribute(jobName, cron, shardingTotalCount);
        //job 核心配置
        JobCoreConfiguration jcc = JobCoreConfiguration
                .newBuilder(jobName,cron,shardingTotalCount)
                .build();
        //job 类配置
        JobTypeConfiguration jtc = new DataflowJobConfiguration(jcc, instance.getClass().getCanonicalName(),streamingProcess);

        //job 根的配置 （LiteJobConfiguration）
        LiteJobConfiguration configuration =  LiteJobConfiguration
                .newBuilder(jtc)
                //覆盖配置
                .overwrite(override)
                .build();
         new SpringJobScheduler((ElasticJob) instance,getZkCenter(),configuration).init();
    }


}

```

重构设计还欠缺很多
