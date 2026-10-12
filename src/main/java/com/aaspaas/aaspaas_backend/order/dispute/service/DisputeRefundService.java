package com.aaspaas.aaspaas_backend.order.dispute.service;

import com.aaspaas.aaspaas_backend.order.dispute.dto.DisputeRefundExecutionResponse;

public interface DisputeRefundService {

    DisputeRefundExecutionResponse executeRefund(Long disputeId);
}
