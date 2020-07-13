package com.zx.server.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-11
 * 秒杀数据传输对象
 */
@Data
@ToString
public class KillDto implements Serializable {

    @NotNull
    private Integer killId;

    private Integer userId;
}
