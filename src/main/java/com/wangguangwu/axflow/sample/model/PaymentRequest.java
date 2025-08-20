package com.wangguangwu.axflow.sample.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付请求基类
 *
 * @author wangguangwu
 */
@Data
public class PaymentRequest {

    /**
     * 支付金额
     */
    @NotBlank(message = "支付金额不能为空")
    private String amount;

}