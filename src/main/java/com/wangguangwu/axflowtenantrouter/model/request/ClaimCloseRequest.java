package com.wangguangwu.axflowtenantrouter.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 理赔关闭请求基类
 * <p>
 * 包含所有租户共享的基本字段和验证规则
 *
 * @author wangguangwu
 */
@Data
public class ClaimCloseRequest {

    /**
     * 案件编号
     */
    @NotBlank(message = "caseCode不能为空")
    private String caseCode;

    /**
     * 关闭原因
     */
    @NotBlank(message = "closeReason不能为空")
    private String closeReason;
}
