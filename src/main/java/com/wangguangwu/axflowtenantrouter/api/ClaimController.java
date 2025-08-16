package com.wangguangwu.axflowtenantrouter.api;

import com.wangguangwu.axflowtenantrouter.annotation.TenantBody;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimAcknowledgeRequest;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimCloseRequest;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
import com.wangguangwu.axflowtenantrouter.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 理赔控制器
 * <p>
 * 提供理赔相关的API接口，支持多租户请求处理
 *
 * @author wangguangwu
 */
@RestController
@RequestMapping("/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    /**
     * 理赔申请接口
     * <p>
     * 根据租户ID动态绑定到对应的请求类型
     *
     * @param request 理赔申请请求（会根据租户ID动态绑定到具体子类）
     * @return 处理结果
     */
    @PostMapping("/intake")
    public String intake(@Valid @TenantBody ClaimIntakeRequest request) {
        return claimService.intake(request);
    }

    /**
     * 理赔确认接口
     * <p>
     * 根据租户ID动态绑定到对应的请求类型
     *
     * @param request 理赔确认请求（会根据租户ID动态绑定到具体子类）
     * @return 处理结果
     */
    @PostMapping("/acknowledge")
    public String acknowledge(@Valid @TenantBody ClaimAcknowledgeRequest request) {
        return claimService.acknowledge(request);
    }

    /**
     * 理赔关闭接口
     * <p>
     * 根据租户ID动态绑定到对应的请求类型
     *
     * @param request 理赔关闭请求（会根据租户ID动态绑定到具体子类）
     * @return 处理结果
     */
    @PostMapping("/close")
    public String close(@Valid @TenantBody ClaimCloseRequest request) {
        return claimService.close(request);
    }
}
