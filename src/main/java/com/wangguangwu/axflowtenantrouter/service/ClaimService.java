package com.wangguangwu.axflowtenantrouter.service;

import com.wangguangwu.axflowtenantrouter.model.request.ClaimSignRequest;
import org.springframework.stereotype.Service;

/**
 * 理赔服务类
 * <p>
 * 处理理赔相关的业务逻辑
 *
 * @author wangguangwu
 */
@Service
public class ClaimService {

    /**
     * 处理理赔申请
     *
     * @param request 理赔申请请求
     * @return 处理结果
     */
    public String sign(ClaimSignRequest request) {
        return "INTAKE_OK:" + request.getCaseCode();
    }
}
