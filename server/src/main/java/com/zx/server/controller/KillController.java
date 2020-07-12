package com.zx.server.controller;

import com.zx.api.enums.StatusCode;
import com.zx.api.response.BaseResponse;
import com.zx.model.dto.KillSuccessUserInfo;
import com.zx.model.mapper.ItemKillSuccessMapper;
import com.zx.server.dto.KillDto;
import com.zx.server.service.IKillService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author ZhangXiong
 * @version v12.0.1
 * @date 2020-07-11
 */
@Controller
public class KillController {

    private static final Logger log = LoggerFactory.getLogger(KillController.class);

    private static final String prefix = "kill";

    @Autowired
    private IKillService killService;

    @Autowired(required = false)
    private ItemKillSuccessMapper itemKillSuccessMapper;

    /***
     * 商品秒杀核心业务逻辑
     * @param dto
     * @param result
     * @return
     */
    @RequestMapping(value = prefix + "/execute", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse execute(@RequestBody @Validated KillDto dto, BindingResult result, HttpSession session) {
        if (result.hasErrors() || dto.getKillId() <= 0) {
            return new BaseResponse(StatusCode.InvalidParams);
        }
        Object uId = session.getAttribute("uid");
        if (uId == null) {
            return new BaseResponse(StatusCode.UserNotLogin);
        }
//        Integer userId = dto.getUserId();
        Integer userId = (Integer) uId;

        BaseResponse response = new BaseResponse(StatusCode.Success);
        try {
            Boolean res = killService.killItem(dto.getKillId(), userId);
            if (!res) {
                return new BaseResponse(StatusCode.Fail.getCode(), "哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }
        } catch (Exception e) {
            response = new BaseResponse(StatusCode.Fail.getCode(), e.getMessage());
        }
        return response;
    }

    /***
     * 商品秒杀核心业务逻辑-用于压力测试
     * @param dto
     * @param result
     * @return
     */
    @RequestMapping(value = prefix + "/execute/lock", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public BaseResponse executeLock(@RequestBody @Validated KillDto dto, BindingResult result) {
        if (result.hasErrors() || dto.getKillId() <= 0) {
            return new BaseResponse(StatusCode.InvalidParams);
        }
        BaseResponse response = new BaseResponse(StatusCode.Success);
        try {
            //不加分布式锁
            /*Boolean res = killService.killItemV2(dto.getKillId(), dto.getUserId());
            if (!res) {
                return new BaseResponse(StatusCode.Fail.getCode(), "不加分布式锁-哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }*/

            //基于Redis的分布式锁进行控制
            /*Boolean res = killService.killItemV3(dto.getKillId(), dto.getUserId());
            if (!res) {
                return new BaseResponse(StatusCode.Fail.getCode(), "基于Redis的分布式锁进行控制-哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }*/

            //基于Redisson的分布式锁进行控制
            /*Boolean res = killService.killItemV4(dto.getKillId(), dto.getUserId());
            if (!res) {
                return new BaseResponse(StatusCode.Fail.getCode(), "基于Redisson的分布式锁进行控制-哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }*/

            //基于ZooKeeper的分布式锁进行控制
            Boolean res = killService.killItemV5(dto.getKillId(), dto.getUserId());
            if (!res) {
                return new BaseResponse(StatusCode.Fail.getCode(), "基于ZooKeeper的分布式锁进行控制-哈哈~商品已抢购完毕或者不在抢购时间段哦!");
            }

        } catch (Exception e) {
            response = new BaseResponse(StatusCode.Fail.getCode(), e.getMessage());
        }
        return response;
    }


    /**
     * 查看订单详情，发邮件给用户中的连接，生成对应订单的页面
     *
     * @return
     */
    @RequestMapping(value = prefix + "/record/detail/{orderNo}", method = RequestMethod.GET)
    public String killRecordDetail(@PathVariable String orderNo, ModelMap modelMap) {
        if (StringUtils.isBlank(orderNo)) {
            return "error";
        }
        KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderNo);
        if (info == null) {
            return "error";
        }
        modelMap.put("info", info);
        return "killRecord";
    }


    //抢购成功跳转页面
    @RequestMapping(value = prefix + "/execute/success", method = RequestMethod.GET)
    public String executeSuccess() {
        return "executeSuccess";
    }

    //抢购失败跳转页面
    @RequestMapping(value = prefix + "/execute/fail", method = RequestMethod.GET)
    public String executeFail() {
        return "executeFail";
    }
}
