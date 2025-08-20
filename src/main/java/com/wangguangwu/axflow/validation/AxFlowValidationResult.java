package com.wangguangwu.axflow.validation;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 业务校验结果。
 *
 * @author wangguangwu
 */
@Getter
public final class AxFlowValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private AxFlowValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static AxFlowValidationResult ok() {
        return new AxFlowValidationResult(true, Collections.emptyList());
    }

    public static AxFlowValidationResult fail(List<String> errors) {
        return new AxFlowValidationResult(false, errors);
    }

}
