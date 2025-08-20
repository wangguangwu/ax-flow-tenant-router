package com.wangguangwu.axflow.sample.controller;

import com.wangguangwu.axflow.annotation.AxFlow;
import com.wangguangwu.axflow.dto.ApiResult;
import com.wangguangwu.axflow.sample.model.PaymentRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 支付控制器
 * <p>
 * 演示如何根据租户自动路由到不同的支付请求实体。
 *
 * @author wangguangwu
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    /**
     * 提交支付请求（允许 TenantA 和 TenantB）
     */
    @AxFlow(allowedTenants = {"TenantA", "TenantB"})
    @PostMapping("/submit")
    public ApiResult<?> submitPayment(PaymentRequest request) {
        return ApiResult.success(request);
    }

    /**
     * 仅允许 TenantA 的支付请求
     */
    @AxFlow(allowedTenants = {"TenantA"})
    @PostMapping("/submit/onlyA")
    public ApiResult<?> submitOnlyATenant(PaymentRequest request) {
        return ApiResult.success(request);
    }

    /**
     * 禁止 TenantA 的支付请求
     */
    @AxFlow(deniedTenants = {"TenantA"})
    @PostMapping("/submit/denyA")
    public ApiResult<?> submitDenyATenant(PaymentRequest request) {
        return ApiResult.success(request);
    }
}
