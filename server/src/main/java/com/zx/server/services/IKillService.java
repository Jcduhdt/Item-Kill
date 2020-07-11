package com.zx.server.services;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-11
 */
public interface IKillService {

    Boolean killItem(Integer killId,Integer userId) throws Exception;

    Boolean killItemV2(Integer killId, Integer userId) throws Exception;

    Boolean killItemV3(Integer killId, Integer userId) throws Exception;

    Boolean killItemV4(Integer killId, Integer userId) throws Exception;

    Boolean killItemV5(Integer killId, Integer userId) throws Exception;
}
