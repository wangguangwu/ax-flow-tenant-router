package com.wangguangwu.axflow.sample.model;

import com.wangguangwu.axflow.annotation.AxFlowModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付请求 - 支付宝版（TenantA）。
 *
 * @author wangguangwu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AxFlowModel("TenantA")
public class AliPayRequest extends PaymentRequest {

    /**
     * 支付宝商户号
     */
    @NotBlank(message = "AliPay: sellerId 不能为空")
    private String sellerId;

    /**
     * 支付宝应用 ID
     */
    @NotBlank(message = "AliPay: appId 不能为空")
    private String appId;

}
