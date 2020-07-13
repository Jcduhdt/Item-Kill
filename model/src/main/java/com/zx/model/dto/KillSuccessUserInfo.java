package com.zx.model.dto;

import com.zx.model.entity.ItemKillSuccess;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-10
 * 秒杀成功用户的信息
 */
@Data
public class KillSuccessUserInfo extends ItemKillSuccess implements Serializable{

    private String userName;

    private String phone;

    private String email;

    private String itemName;

    @Override
    public String toString() {
        return super.toString()+"\nKillSuccessUserInfo{" +
                "userName='" + userName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", itemName='" + itemName + '\'' +
                '}';
    }
}