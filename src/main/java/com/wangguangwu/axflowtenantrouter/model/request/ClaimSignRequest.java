package com.wangguangwu.axflowtenantrouter.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 理赔申请请求基类
 * <p>
 * 包含所有租户共享的基本字段和验证规则
 *
 * @author wangguangwu
 */
@Data
public class ClaimSignRequest {

    /**
     * 案件编号
     */
    @NotBlank(message = "caseCode不能为空")
    private String caseCode;

    /**
     * 图片路径列表
     */
    @NotEmpty(message = "imagePathList不能为空")
    private List<@NotBlank String> imagePathList;
}
