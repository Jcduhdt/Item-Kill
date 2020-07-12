package com.zx.server.service.impl;

import com.zx.model.entity.ItemKill;
import com.zx.model.entity.ItemKillSuccess;
import com.zx.model.mapper.ItemKillMapper;
import com.zx.model.mapper.ItemKillSuccessMapper;
import com.zx.server.enums.SysConstant;
import com.zx.server.service.IKillService;
import com.zx.server.service.RabbitSenderService;
import com.zx.server.utils.RandomUtil;
import com.zx.server.utils.SnowFlake;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.joda.time.DateTime;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-11
 */
@Service
public class KillService implements IKillService {

    private static final Logger log = LoggerFactory.getLogger(KillService.class);

    private SnowFlake snowFlake = new SnowFlake(2, 3);

    @Autowired(required = false)
    private ItemKillSuccessMapper itemKillSuccessMapper;

    @Autowired(required = false)
    private ItemKillMapper itemKillMapper;

    @Autowired
    private RabbitSenderService rabbitSenderService;

    /**
     * 商品秒杀核心业务逻辑的处理
     *
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItem(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        // 判断当前用户是否已经抢购过当前商品
        if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
            // 查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectById(killId);

            // 判断是否可以被秒杀canKill=1?
            if (itemKill != null && 1 == itemKill.getCanKill()) {
                // 扣减库存-减一
                int res = itemKillMapper.updateKillItem(killId);

                // 扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if (res > 0) {
                    commonRecordKillSuccessInfo(itemKill, userId);
                    result = true;
                }
            }
        } else {
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }

    /**
     * 通用的方法-记录用户秒杀成功后生成的订单-并进行异步邮件消息的通知
     *
     * @param kill
     * @param userId
     * @throws Exception
     */
    private void commonRecordKillSuccessInfo(ItemKill kill, Integer userId) throws Exception {
        // 记录抢购成功后生成的秒杀订单记录

        ItemKillSuccess entity = new ItemKillSuccess();
        String orderNo = String.valueOf(snowFlake.nextId());

//         entity.setCode(RandomUtil.generateOrderCode());   //传统时间戳+N位随机数
        entity.setCode(orderNo); //雪花算法
        entity.setItemId(kill.getItemId());
        entity.setKillId(kill.getId());
        entity.setUserId(userId.toString());
        entity.setStatus(SysConstant.OrderStatus.SuccessNotPayed.getCode().byteValue());
        entity.setCreateTime(DateTime.now().toDate());
        // 学以致用，举一反三 -> 仿照单例模式的双重检验锁写法
        // 查完更新还是会出差错
        if (itemKillSuccessMapper.countByKillUserId(kill.getId(), userId) <= 0) {
            int res = itemKillSuccessMapper.insertSelective(entity);
            if (res > 0) {
                // 进行异步邮件消息的通知=rabbitmq+mail
                rabbitSenderService.sendKillSuccessEmailMsg(orderNo);

                // 入死信队列，用于 “失效” 超过指定的TTL时间时仍然未支付的订单
                rabbitSenderService.sendKillSuccessOrderExpireMsg(orderNo);
            }
        }
    }


    /**
     * 商品秒杀核心业务逻辑的处理-mysql的优化
     * 该方法还是会导致多线程下，同一个用户抢到多本同样的书
     *
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV2(Integer killId, Integer userId) throws Exception {
        Boolean result = false;
        // 判断当前用户是否已经抢购过当前商品
        if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
            // A.查询待秒杀商品详情
            ItemKill itemKill = itemKillMapper.selectByIdV2(killId);

            // 判断是否可以被秒杀canKill=1?
            if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                // B.扣减库存-减一
                int res = itemKillMapper.updateKillItemV2(killId);

                // 扣减是否成功?是-生成秒杀成功的订单，同时通知用户秒杀成功的消息
                if (res > 0) {
                    commonRecordKillSuccessInfo(itemKill, userId);
                    result = true;
                }
            }
        } else {
            throw new Exception("您已经抢购过该商品了!");
        }
        return result;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 商品秒杀核心业务逻辑的处理-redis的分布式锁
     *
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV3(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
            // 借助Redis的原子操作实现分布式锁-对共享操作-资源进行控制
            ValueOperations valueOperations = stringRedisTemplate.opsForValue();
            // 构造key
            final String key = new StringBuffer().append(killId).append(userId).append("-RedisLock").toString();
            // 构造value，没有实际含义，只是为了存储key,且释放锁的时候通过value判断
            final String value = RandomUtil.generateOrderCode();
            // 设置锁，通过setnx设置锁，返回该锁是否存在(有效)
            Boolean cacheRes = valueOperations.setIfAbsent(key, value); //luna脚本提供“分布式锁服务”，就可以写在一起
            // redis部署节点宕机了，会出现锁死的情况
            if (cacheRes) {
                // 设置30s后释放锁
                stringRedisTemplate.expire(key, 30, TimeUnit.SECONDS);
                try {
                    // 查询
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                        // 更新
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if (res > 0) {
                            // 记录
                            commonRecordKillSuccessInfo(itemKill, userId);
                            result = true;
                        }
                    }
                } catch (Exception e) {
                    throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕！");
                } finally {
                    if (value.equals(valueOperations.get(key).toString())) {
                        stringRedisTemplate.delete(key);
                    }
                }
            }
        } else {
            throw new Exception("Redis-您已经抢购过该商品了!");
        }
        return result;
    }

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 商品秒杀核心业务逻辑的处理-redisson的分布式锁
     *
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV4(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        final String lockKey = new StringBuffer().append(killId).append(userId).append("-RedissonLock").toString();
        // 唯一的key获得锁
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 人性化时间，可重入，30s尝试，获取锁后10s释放
            Boolean cacheRes = lock.tryLock(30, 10, TimeUnit.SECONDS);
            if (cacheRes) {
                // 核心业务逻辑的处理
                if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if (res > 0) {
                            commonRecordKillSuccessInfo(itemKill, userId);

                            result = true;
                        }
                    }
                } else {
                    throw new Exception("redisson-您已经抢购过该商品了!");
                }
            }
        } finally {
            lock.unlock();
            // 强制释放
            //lock.forceUnlock();
        }
        return result;
    }

    @Autowired
    private CuratorFramework curatorFramework;

    // 路径，zookeeper要求以/开头
    private static final String pathPrefix = "/kill/zkLock/";

    /**
     * 商品秒杀核心业务逻辑的处理-基于ZooKeeper的分布式锁
     * 创建接结点有开销
     * @param killId
     * @param userId
     * @return
     * @throws Exception
     */
    @Override
    public Boolean killItemV5(Integer killId, Integer userId) throws Exception {
        Boolean result = false;

        // 进程之间的互斥操作锁
        InterProcessMutex mutex = new InterProcessMutex(curatorFramework, pathPrefix + killId + userId + "-lock");
        try {
            if (mutex.acquire(10L, TimeUnit.SECONDS)) {
                // 核心业务逻辑
                if (itemKillSuccessMapper.countByKillUserId(killId, userId) <= 0) {
                    ItemKill itemKill = itemKillMapper.selectByIdV2(killId);
                    if (itemKill != null && 1 == itemKill.getCanKill() && itemKill.getTotal() > 0) {
                        int res = itemKillMapper.updateKillItemV2(killId);
                        if (res > 0) {
                            commonRecordKillSuccessInfo(itemKill, userId);
                            result = true;
                        }
                    }
                } else {
                    throw new Exception("zookeeper-您已经抢购过该商品了!");
                }
            }
        } catch (Exception e) {
            throw new Exception("还没到抢购日期、已过了抢购时间或已被抢购完毕！");
        } finally {
            if (mutex != null) {
                mutex.release();
            }
        }
        return result;
    }
}
