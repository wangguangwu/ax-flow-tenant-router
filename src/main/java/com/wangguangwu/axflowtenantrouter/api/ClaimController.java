package com.wangguangwu.axflowtenantrouter.api;

import com.wangguangwu.axflowtenantrouter.model.request.ClaimSignRequest;
import com.wangguangwu.axflowtenantrouter.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
@RequestMapping("/claim")
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
    @PostMapping("/sign")
    public String sign(@RequestBody @Valid ClaimSignRequest request) {
        return claimService.sign(request);
    }
}
