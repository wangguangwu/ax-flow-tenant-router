package com.wangguangwu.axflowtenantrouter.model.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 理赔确认请求基类
 * <p>
 * 包含所有租户共享的基本字段和验证规则
 *
 * @author wangguangwu
 */
@Data
public class ClaimAcknowledgeRequest {

    /**
     * 案件编号
     */
    @NotBlank(message = "caseCode不能为空")
    private String caseCode;

    /**
     * 确认人
     */
    @NotBlank(message = "ackBy不能为空")
    private String ackBy;
}
