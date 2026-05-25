package com.easymusic.controller;

import com.easymusic.entity.query.PayOrderInfoQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.PayOrderInfoService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@Slf4j
public class PayOrderController extends ABaseController {

    @Resource
    private PayOrderInfoService payOrderInfoService;

    @RequestMapping("/loadOrder")
    public ResponseVO loadOrder(PayOrderInfoQuery orderInfoQuery) {
        orderInfoQuery.setOrderBy("p.create_time desc");
        orderInfoQuery.setQueryUser(true);
        PaginationResultVO resultVO = payOrderInfoService.findListByPage(orderInfoQuery);
        return getSuccessResponseVO(resultVO);
    }
}
