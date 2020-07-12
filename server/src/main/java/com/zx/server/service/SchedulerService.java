package com.zx.server.service;

import com.zx.model.entity.ItemKillSuccess;
import com.zx.model.mapper.ItemKillSuccessMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-11
 * 定时任务
 */
@Service
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);

    @Autowired(required = false)
    private ItemKillSuccessMapper itemKillSuccessMapper;

    /**
     * 定时获取status=0的订单并判断是否超过TTL，然后进行失效
     */
    // 在线cron表达式生成器 https://cron.qqe2.com/
    // 10s
//    @Scheduled(cron = "0/10 * * * * ?")
    // 30min
    @Scheduled(cron = "0 0/30 * * * ?")
    public void schedulerExpireOrders(){
        //log.info("v1的定时任务----");

        try {
            List<ItemKillSuccess> list=itemKillSuccessMapper.selectExpireOrders();
            if (list!=null && !list.isEmpty()){
                //java8的写法
                list.stream().forEach(i -> {
                    if (i!=null && i.getDiffTime() > env.getProperty("scheduler.expire.orders.time",Integer.class)){
                        itemKillSuccessMapper.expireOrder(i.getCode());
                    }
                });
            }

            /*for (ItemKillSuccess entity:list){
            }*/ //非java8的写法
        }catch (Exception e){
            log.error("定时获取status=0的订单并判断是否超过TTL，然后进行失效-发生异常：",e.fillInStackTrace());
        }
    }

    @Autowired
    private Environment env;


    // 测试线程池处理定时任务
    /*@Scheduled(cron = "0/11 * * * * ?")
    public void schedulerExpireOrdersV2(){
        log.info("v2的定时任务----");
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void schedulerExpireOrdersV3(){
        log.info("v3的定时任务----");
    }*/
}
