package com.wangguangwu.axflowtenantrouter.service;

import com.wangguangwu.axflowtenantrouter.model.common.ClaimAcknowledgeRequest;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimCloseRequest;
import com.wangguangwu.axflowtenantrouter.model.common.ClaimIntakeRequest;
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
    public String intake(ClaimIntakeRequest request) {
        return "INTAKE_OK:" + request.getCaseCode();
    }

    /**
     * 处理理赔确认
     *
     * @param request 理赔确认请求
     * @return 处理结果
     */
    public String acknowledge(ClaimAcknowledgeRequest request) {
        return "ACK_OK:" + request.getCaseCode();
    }

    /**
     * 处理理赔关闭
     *
     * @param request 理赔关闭请求
     * @return 处理结果
     */
    public String close(ClaimCloseRequest request) {
        return "CLOSE_OK:" + request.getCaseCode();
    }
}
