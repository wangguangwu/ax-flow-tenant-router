package com.wangguangwu.axflowtenantrouter.model.common;

import lombok.Getter;

import java.util.List;

/**
 * 验证结果模型
 * <p>
 * 用于封装验证结果和错误信息
 *
 * @author wangguangwu
 */
@Getter
public class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    /**
     * 创建验证成功的结果
     *
     * @return 验证成功结果
     */
    public static ValidationResult success() {
        return new ValidationResult(true, List.of());
    }

    /**
     * 创建验证失败的结果
     *
     * @param errors 错误信息列表
     * @return 验证失败结果
     */
    public static ValidationResult fail(List<String> errors) {
        return new ValidationResult(false, errors);
    }
}
