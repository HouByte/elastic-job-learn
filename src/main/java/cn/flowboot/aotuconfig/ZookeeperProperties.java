package cn.flowboot.aotuconfig;

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
    private int baseSleepTimeMilliseconds = 1000;
    /**
     * 等待重试的间隔时间的最大值 默认3000，单位：毫秒
     */
    private int maxSleepTimeMilliseconds = 3000;
    /**
     * 最大重试次数 默认3
     */
    private int maxRetries = 3;
    /**
     * 会话超时时间 默认60000，单位：毫秒
     */
    private int sessionTimeoutMilliseconds = 60000;
    /**
     * 连接超时时间 默认15000，单位：毫秒
     */
    private int  connectionTimeoutMilliseconds = 15000;
}
