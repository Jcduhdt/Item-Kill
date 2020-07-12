package com.zx.server.service;

import com.zx.model.entity.ItemKill;

import java.util.List;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-10
 */
public interface IItemService {
    List<ItemKill> getKillItems() throws Exception;

    ItemKill getKillDetail(Integer id) throws Exception;
}
