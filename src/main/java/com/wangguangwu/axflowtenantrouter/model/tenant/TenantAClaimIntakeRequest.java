package com.wangguangwu.axflowtenantrouter.model.tenant;

import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TenantA特定的理赔申请请求
 * <p>
 * 扩展基本理赔申请请求，添加TenantA特定的字段和验证规则
 *
 * @author wangguangwu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantAClaimIntakeRequest extends ClaimIntakeRequest {

    /**
     * 被保险人姓名
     */
    @NotBlank(message = "insuredName不能为空")
    private String insuredName;
}
