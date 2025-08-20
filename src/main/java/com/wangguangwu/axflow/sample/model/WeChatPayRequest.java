package com.wangguangwu.axflow.sample.model;

import com.wangguangwu.axflow.annotation.AxFlowModel;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付请求 - 微信版（TenantB）。
 *
 * @author wangguangwu
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AxFlowModel("TenantB")
public class WeChatPayRequest extends PaymentRequest {

    /**
     * 微信商户号
     */
    @NotBlank(message = "WeChatPay: mchId 不能为空")
    private String mchId;

    /**
     * 微信 AppId
     */
    @NotBlank(message = "WeChatPay: appId 不能为空")
    private String appId;

}