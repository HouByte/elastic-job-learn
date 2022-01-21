package cn.flowboot.simple.job;

import cn.flowboot.aotuconfig.ElasticDataflowJob;
import cn.flowboot.simple.model.DataflowOrder;
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
//@ElasticDataflowJob(name = "myDataflowJob",cron = "0/10 * * * * ?",shardingTotalCount = 2,override = true,streamingProcess = true)
public class MyDataflowJob implements DataflowJob<DataflowOrder> {

    private List<DataflowOrder> dataflowOrders = new ArrayList<>();

    /**
     * 模拟订单数据
     */
    {
        for (int i = 0; i < 100; i++) {
            DataflowOrder dataflowOrder = new DataflowOrder();
            dataflowOrder.setOrderId(i+1);
            dataflowOrder.setStatus(0);
            dataflowOrders.add(dataflowOrder);
        }
    }

    @Override
    public List<DataflowOrder> fetchData(ShardingContext shardingContext) {
        //订单号 % 分片总数 == 当前分片项
        List<DataflowOrder> dataflowOrderList = dataflowOrders.stream().filter(o -> o.getStatus() == 0)
                .filter(o -> o.getOrderId() % shardingContext.getShardingTotalCount() == shardingContext.getShardingItem())
                .collect(Collectors.toList());

        //只处理前10个
        if (dataflowOrderList.size() >0){
            dataflowOrderList = dataflowOrderList.subList(0,10);
        }
        //模拟耗时
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("DataflowJob %s 当前分片项 %d,抓取数据是 %s\n", LocalTime.now(),shardingContext.getShardingItem(), dataflowOrderList);

        return dataflowOrderList;
    }

    @Override
    public void processData(ShardingContext shardingContext, List<DataflowOrder> list) {
        System.out.printf("DataflowJob %s 当前分片项 %d 正在处理数据...\n", LocalTime.now(),shardingContext.getShardingItem());
        list.forEach(o->o.setStatus(1));
        System.out.println(list+"\n"+ dataflowOrders);
        //模拟耗时
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
