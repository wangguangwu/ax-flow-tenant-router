package com.wangguangwu.axflow.sample.validation;

import com.wangguangwu.axflow.sample.model.WeChatPayRequest;
import com.wangguangwu.axflow.validation.AxFlowValidationResult;
import com.wangguangwu.axflow.validation.AxFlowValidator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信业务校验：示例逻辑
 * 1) mchId 必须以 'W' 开头
 * 2) appId 必须以 'W' 开头
 * 3) mchId 与 appId 不能相同
 *
 * @author wangguangwu
 */
@Component
@Order(100)
public class WeChatPayValidator implements AxFlowValidator<WeChatPayRequest> {

    @Override
    public Class<WeChatPayRequest> targetType() {
        return WeChatPayRequest.class;
    }

    @Override
    public AxFlowValidationResult validate(WeChatPayRequest v) {
        List<String> errors = new ArrayList<>();

        // 1) 首字母校验
        if (v.getMchId() != null && !v.getMchId().startsWith("W")) {
            errors.add("WeChatPay: mchId 必须以 'W' 开头");
        }
        if (v.getAppId() != null && !v.getAppId().startsWith("W")) {
            errors.add("WeChatPay: appId 必须以 'W' 开头");
        }

        // 2) 互异性校验
        if (v.getMchId() != null && v.getAppId() != null
                && v.getMchId().equals(v.getAppId())) {
            errors.add("WeChatPay: mchId 与 appId 不能相同");
        }

        return errors.isEmpty() ? AxFlowValidationResult.ok()
                : AxFlowValidationResult.fail(errors);
    }
}
