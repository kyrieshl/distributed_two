package com.litemall.distributed_two.web;

import com.litemall.distributed_two.annotation.LoginAdmin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.linlinjava.litemall.db.domain.LitemallOrder;
import org.linlinjava.litemall.db.service.LitemallOrderService;
import org.linlinjava.litemall.db.util.OrderUtil;
import org.linlinjava.litemall.db.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/order")
public class OrderController {
    private final Log logger = LogFactory.getLog(OrderController.class);

    @Autowired
    private LitemallOrderService orderService;

    @GetMapping("/list")
    public Object list(@LoginAdmin Integer adminId,
                       Integer userId, String orderSn,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "limit", defaultValue = "10") Integer limit,
                       String sort, String order){
        if(adminId == null){
            return ResponseUtil.fail401();
        }
        List<LitemallOrder> orderList = orderService.querySelective(userId, orderSn, page, limit, sort, order);
        int total = orderService.countSelective(userId, orderSn, page, limit, sort, order);

        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", orderList);

        return ResponseUtil.ok(data);
    }

    /*
     * 目前的逻辑不支持管理员创建
     */
    @PostMapping("/create")
    public Object create(@LoginAdmin Integer adminId, @RequestBody LitemallOrder order){
        if(adminId == null){
            return ResponseUtil.unlogin();
        }
        return ResponseUtil.unsupport();
    }

    @GetMapping("/read")
    public Object read(@LoginAdmin Integer adminId, Integer id){
        if(adminId == null){
            return ResponseUtil.fail401();
        }

        LitemallOrder order = orderService.findById(id);
        return ResponseUtil.ok(order);
    }

    /*
     * 目前仅仅支持管理员设置发货相关的信息
     */
    @PostMapping("/update")
    public Object update(@LoginAdmin Integer adminId, @RequestBody LitemallOrder order){
        if(adminId == null){
            return ResponseUtil.unlogin();
        }

        Integer orderId = order.getId();
        if(orderId == null){
            return ResponseUtil.badArgument();
        }

        LitemallOrder zmallOrder = orderService.findById(orderId);
        if(zmallOrder == null){
            return ResponseUtil.badArgumentValue();
        }

        if(OrderUtil.isPayStatus(zmallOrder) || OrderUtil.isShipStatus(zmallOrder)){
            LitemallOrder newOrder = new LitemallOrder();
            String s = order.getShipChannel();
            if(order.getShipChannel().equals("顺丰")){
                s = "SF";
            }
            if (order.getShipChannel().equals("EMS")){
                s = "EMS";
            }
            if(order.getShipChannel().equals("百世快递")){
                s = "HTKY";
            }
            if(order.getShipChannel().equals("中通快递")){
                s = "ZTO";
            }
            if(order.getShipChannel().equals("申通快递")){
                s = "STO";
            }
            if(order.getShipChannel().equals("圆通快递")){
                s = "YTO";
            }
            if(order.getShipChannel().equals("韵达快递")){
                s = "YD";
            }
            if(order.getShipChannel().equals("邮政包裹")){
                s = "YZPY";
            }
            if(order.getShipChannel().equals("天天快递")){
                s = "HHTT";
            }
            if(order.getShipChannel().equals("京东物流")){
                s = "JD";
            }
            newOrder.setId(orderId);
            newOrder.setShipChannel(s);
            newOrder.setShipSn(order.getShipSn());
            newOrder.setShipStartTime(order.getShipStartTime());
            newOrder.setShipEndTime(order.getShipEndTime());
            newOrder.setOrderStatus(OrderUtil.STATUS_SHIP);
            orderService.update(newOrder);
        }
        else {
            return ResponseUtil.badArgumentValue();
        }

        zmallOrder = orderService.findById(orderId);
        return ResponseUtil.ok(zmallOrder);
    }

    @PostMapping("/delete")
    public Object delete(@LoginAdmin Integer adminId, @RequestBody LitemallOrder order){
        if(adminId == null){
            return ResponseUtil.unlogin();
        }
        return ResponseUtil.unsupport();
    }

}
