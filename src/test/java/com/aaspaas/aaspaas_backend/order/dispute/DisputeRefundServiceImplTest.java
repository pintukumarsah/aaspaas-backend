package com.aaspaas.aaspaas_backend.order.dispute;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.order.dispute.dto.DisputeRefundExecutionResponse;
import com.aaspaas.aaspaas_backend.order.dispute.entity.OrderDispute;
import com.aaspaas.aaspaas_backend.order.dispute.repository.OrderDisputeRepository;
import com.aaspaas.aaspaas_backend.order.dispute.serviceimpl.DisputeRefundServiceImpl;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.payment.repository.PaymentRefundRepository;
import com.aaspaas.aaspaas_backend.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DisputeRefundServiceImplTest {

    @Mock OrderDisputeRepository disputeRepository;
    @Mock PaymentRefundRepository paymentRefundRepository;
    @Mock PaymentService paymentService;

    @InjectMocks DisputeRefundServiceImpl service;

    private OrderDispute dispute;

    @BeforeEach
    void setUp() {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(50L);
        when(order.getTotalAmount()).thenReturn(new BigDecimal("300.00"));

        dispute = new OrderDispute();
        dispute.setId(1L);
        dispute.setOrder(order);
        dispute.setStatus("RESOLVED");
        dispute.setResolutionCode("REFUND_RECOMMENDED");
        dispute.setApprovedRefundAmount(new BigDecimal("300.00"));

        when(disputeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(dispute));
        when(paymentRefundRepository.existsByOrderId(50L)).thenReturn(false);
    }

    @Test
    void fullApprovedRefundIsSentToPaymentPipeline() {
        DisputeRefundExecutionResponse res = service.executeRefund(1L);
        assertEquals(50L, res.getOrderId());
        verify(paymentService).initiateRefund(eq(50L), anyString());
    }

    @Test
    void unresolvedDisputeIsRejected() {
        dispute.setStatus("UNDER_REVIEW");
        assertEquals(409, assertThrows(BusinessException.class,
                () -> service.executeRefund(1L)).getStatusCode());
        verifyNoInteractions(paymentService);
    }

    @Test
    void partialAmountIsRejected() {
        dispute.setApprovedRefundAmount(new BigDecimal("100.00"));
        assertEquals(409, assertThrows(BusinessException.class,
                () -> service.executeRefund(1L)).getStatusCode());
        verifyNoInteractions(paymentService);
    }

    @Test
    void secondExecutionIsRejected() {
        when(paymentRefundRepository.existsByOrderId(50L)).thenReturn(true);
        assertEquals(409, assertThrows(BusinessException.class,
                () -> service.executeRefund(1L)).getStatusCode());
        verify(paymentService, never()).initiateRefund(any(), any());
    }
}
