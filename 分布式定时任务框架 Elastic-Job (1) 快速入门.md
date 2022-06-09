@[TOC](分布式定时任务框架 Elastic-Job 一 快速入门)
# 介绍
> 以下内容来着官网介绍

> [ElasticJob](https://shardingsphere.apache.org/elasticjob/index_zh.html) 是一个分布式调度解决方案，由 2 个相互独立的子项目 ElasticJob-Lite 和 ElasticJob-Cloud 组成。
ElasticJob-Lite 定位为轻量级无中心化解决方案，使用jar的形式提供分布式任务的协调服务；
ElasticJob-Cloud 使用 Mesos 的解决方案，额外提供资源治理、应用分发以及进程隔离等服务。
ElasticJob 的各个产品使用统一的作业 API，开发者仅需要一次开发，即可随意部署。

> 使用 ElasticJob 能够让开发工程师不再担心任务的线性吞吐量提升等非功能需求，使他们能够更加专注于面向业务编码设计； 同时，它也能够解放运维工程师，使他们不必再担心任务的可用性和相关管理需求，只通过轻松的增加服务节点即可达到自动化运维的目的。

ElasticJob-Lite
定位为轻量级无中心化解决方案，使用 jar 的形式提供分布式任务的协调服务。
![ElasticJob-Lite Architecture](https://img-blog.csdnimg.cn/img_convert/dc415a736aa5a71d9516365b191fd921.png#pic_center)
ElasticJob-Cloud
采用自研 Mesos Framework 的解决方案，额外提供资源治理、应用分发以及进程隔离等功能。
![ElasticJob-Cloud Architecture](https://img-blog.csdnimg.cn/img_convert/67cf58deaec876cc69eb3020eaf2d427.png#pic_center)


功能| ElasticJob-Lite |	ElasticJob-Cloud
-------- | -------- | -----
无中心化 |	是 |	否
资源分配 |不支持 |	支持
作业模式|	常驻	|常驻 + 瞬时
部署依赖|	ZooKeeper|	ZooKeeper + Mesos

功能列表

 - 弹性调度

 	- 支持任务在分布式场景下的分片和高可用
 	- 能够水平扩展任务的吞吐量和执行效率
	 - 任务处理能力随资源配备弹性伸缩
 - 资源分配

	- 在适合的时间将适合的资源分配给任务并使其生效
	 - 相同任务聚合至相同的执行器统一处理
	 - 动态调配追加资源至新分配的任务
 - 作业治理

	 - 失效转移
	 - 错过作业重新执行
	 - 自诊断修复
 - 作业依赖(TODO)

	 - 基于有向无环图（DAG）的作业间依赖
	 - 基于有向无环图（DAG）的作业分片间依赖
 - 作业开放生态

	 - 可扩展的作业类型统一接口
 	- 丰富的作业类型库，如数据流、脚本、HTTP、文件、大数据等
	 - 易于对接业务作业，能够与 Spring 依赖注入无缝整合
 - 可视化管控端
	 - 作业管控端
	 - 作业执行历史数据追踪
 	- 注册中心管理

# 添加依赖
添加elastic-job lite
```xml
<!-- elastic-job-lite-->
<dependency>
    <groupId>com.dangdang</groupId>
    <artifactId>elastic-job-lite-core</artifactId>
    <version>2.1.5</version>
</dependency>
<!-- 用于测试 -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.13</version>
    <scope>test</scope>
</dependency>
```
# 快速入门
Elastic-Job支持Simple、DataflowJob、Script三种作业模式
## Simple 快速入门
### 编写SimpleJob
```java
package cn.flowboot.simple.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;

/**
 * <h1>My Simple Job</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
public class MySimpleJob implements SimpleJob {


    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.printf("SimpleJob %s 当前分片项 %d,总分片项 %d\n", LocalTime.now(),shardingContext.getShardingItem(),shardingContext.getShardingTotalCount());
    }
}

```
### 配置且测试
```java
package cn.flowboot.simple;

import cn.flowboot.simple.job.MySimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.Test;

/**
 * <h1>Simple Job Test</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
public class SimpleJobTest {
    
    public static void main(String[] args) {
        new JobScheduler(zkCenter(),configuration()).init();
    }

    public static CoordinatorRegistryCenter zkCenter(){
        ZookeeperConfiguration zc = new ZookeeperConfiguration("localhost:2181","java-simple-job");
        CoordinatorRegistryCenter crc = new ZookeeperRegistryCenter(zc);
        //初始化
        crc.init();
        return crc;
    }

    /**
     * job 配置
     * @return
     */
    public static LiteJobConfiguration configuration(){
        //job 核心配置
        JobCoreConfiguration jcc = JobCoreConfiguration
                .newBuilder("mySimpleJob","0/5 * * * * ?",2)
                .build();
        //job 类配置
        JobTypeConfiguration jtc = new SimpleJobConfiguration(jcc, MySimpleJob.class.getCanonicalName());
        //job 根的配置 （LiteJobConfiguration）
        return LiteJobConfiguration
                .newBuilder(jtc)
                //覆盖配置
                .overwrite(true)
                .build();
    }
}
```
## DataflowJob 快速入门
### 编写DataflowJob 
```java
package cn.flowboot.simple.job;

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
### 配置且测试
> 与Simple 测试区别在于job 类配置不同
```java
package cn.flowboot.simple;

import cn.flowboot.simple.job.MyDataflowJob;
import cn.flowboot.simple.job.MySimpleJob;
import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.dataflow.DataflowJobConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.junit.Test;

/**
 * <h1>Simple Job Test</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
public class DataflowJobTest {

    public static void main(String[] args) {
        new JobScheduler(zkCenter(),configuration()).init();
    }

    public static CoordinatorRegistryCenter zkCenter(){
        ZookeeperConfiguration zc = new ZookeeperConfiguration("localhost:2181","java-simple-job");
        CoordinatorRegistryCenter crc = new ZookeeperRegistryCenter(zc);
        //初始化
        crc.init();
        return crc;
    }

    /**
     * job 配置
     * @return
     */
    public static LiteJobConfiguration configuration(){
        //job 核心配置
        JobCoreConfiguration jcc = JobCoreConfiguration
                .newBuilder("myDataflowJob","0/5 * * * * ?",2)
                .build();
        //job 类配置
        JobTypeConfiguration jtc = new DataflowJobConfiguration(jcc, MyDataflowJob.class.getCanonicalName(),true);
        //job 根的配置 （LiteJobConfiguration）
        return LiteJobConfiguration
                .newBuilder(jtc)
                //覆盖配置
                .overwrite(true)
                .build();
    }
}

```
## Script 快速入门
脚本作业 支持shell,python,perl等
编写cmd文件
```shell
echo cmd 脚本信息，显示内容: %1
```
配置且测试

```java
package cn.flowboot.simple;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobTypeConfiguration;
import com.dangdang.ddframe.job.config.script.ScriptJobConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;

/**
 * <h1>Simple Job Test</h1>
 *
 * @version 1.0
 * @author: Vincent Vic
 * @since: 2022/01/18
 */
public class ScriptJobTest {

    public static void main(String[] args) {
        new JobScheduler(zkCenter(),configuration()).init();
    }

    public static CoordinatorRegistryCenter zkCenter(){
        ZookeeperConfiguration zc = new ZookeeperConfiguration("localhost:2181","java-simple-job");
        CoordinatorRegistryCenter crc = new ZookeeperRegistryCenter(zc);
        //初始化
        crc.init();
        return crc;
    }

    /**
     * job 配置
     * @return
     */
    public static LiteJobConfiguration configuration(){
        //job 核心配置
        JobCoreConfiguration jcc = JobCoreConfiguration
                .newBuilder("myScriptJob","0/5 * * * * ?",2)
                .build();
        //job 类配置
        JobTypeConfiguration jtc = new ScriptJobConfiguration(jcc, "D:\\test.cmd");
        //job 根的配置 （LiteJobConfiguration）
        return LiteJobConfiguration
                .newBuilder(jtc)
                //覆盖配置
                .overwrite(true)
                .build();
    }
}

```

