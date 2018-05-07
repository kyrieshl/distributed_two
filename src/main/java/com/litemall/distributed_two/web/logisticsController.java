package com.litemall.distributed_two.web;

import com.litemall.distributed_two.service.logisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logistics")
public class logisticsController {
    private logisticsService logisticsService;

    @GetMapping("/logistics")
    public String getLogistcs(@RequestParam String expCode,
                              @RequestParam String expNo) throws Exception {
        return logisticsService.getOrderTracesByJson(expCode,expNo);
    }
}
