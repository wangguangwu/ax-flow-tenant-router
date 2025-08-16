package com.wangguangwu.axflowtenantrouter.model.tenant;

import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * TenantB特定的理赔申请请求
 * <p>
 * 扩展基本理赔申请请求，添加TenantB特定的字段和验证规则
 *
 * @author wangguangwu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TenantBClaimIntakeRequest extends ClaimIntakeRequest {

    /**
     * 主被保险人姓名
     */
    @Size(max = 20, message = "mainInsuredName不能超过20字")
    private String mainInsuredName;

    /**
     * 优先级
     */
    @NotNull(message = "priority不能为空")
    private Integer priority;
}
