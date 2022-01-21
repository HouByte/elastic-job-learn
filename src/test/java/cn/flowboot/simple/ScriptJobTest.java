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
        JobTypeConfiguration jtc = new ScriptJobConfiguration(jcc, "D:\\Code\\project\\java-simple-job\\src\\test\\resources\\test.cmd");
        //job 根的配置 （LiteJobConfiguration）
        return LiteJobConfiguration
                .newBuilder(jtc)
                //覆盖配置
                .overwrite(true)
                .build();
    }
}
