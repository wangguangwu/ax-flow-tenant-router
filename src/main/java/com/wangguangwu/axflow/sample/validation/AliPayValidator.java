package com.wangguangwu.axflow.sample.validation;

import com.wangguangwu.axflow.sample.model.AliPayRequest;
import com.wangguangwu.axflow.validation.AxFlowValidationResult;
import com.wangguangwu.axflow.validation.AxFlowValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * 支付宝业务校验：示例逻辑
 * 1) sellerId 必须以 'A' 开头
 * 2) appId 必须以 'A' 开头
 * 3) sellerId 与 appId 不能相同
 *
 * @author wangguangwu
 */
@Component
@Order(100)
public class AliPayValidator implements AxFlowValidator<AliPayRequest> {

    @Override
    public Class<AliPayRequest> targetType() {
        return AliPayRequest.class;
    }

    @Override
    public AxFlowValidationResult validate(AliPayRequest v) {
        List<String> errors = new ArrayList<>();

        // 1) 首字母校验
        if (v.getSellerId() != null && !v.getSellerId().startsWith("A")) {
            errors.add("AliPay: sellerId 必须以 'A' 开头");
        }
        if (v.getAppId() != null && !v.getAppId().startsWith("A")) {
            errors.add("AliPay: appId 必须以 'A' 开头");
        }

        // 2) 互异性校验
        if (v.getSellerId() != null && v.getAppId() != null
                && v.getSellerId().equals(v.getAppId())) {
            errors.add("AliPay: sellerId 与 appId 不能相同");
        }

        return errors.isEmpty() ? AxFlowValidationResult.ok()
                : AxFlowValidationResult.fail(errors);
    }
}
