package com.aaspaas.aaspaas_backend.delivery.serviceimpl;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.delivery.dto.CreateDeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.dto.DeliveryRequestResponse;
import com.aaspaas.aaspaas_backend.delivery.entity.DeliveryRequest;
import com.aaspaas.aaspaas_backend.delivery.repository.DeliveryRequestRepository;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aaspaas.aaspaas_backend.delivery.service.DeliveryRequestService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryRequestServiceImpl
        implements DeliveryRequestService {

    private final DeliveryRequestRepository
            deliveryRequestRepository;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    @Override
    public DeliveryRequestResponse createRequest(
            CreateDeliveryRequest request,
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        Order order = orderRepository
                .findById(request.getOrderId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Order not found",
                                404
                        )
                );

        /*
         * Only the customer who created the order
         * can create its delivery request.
         */
        if (!order.getCustomer()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not authorized for this order",
                    403
            );
        }

        /*
         * One order can have only one delivery request.
         */
        if (deliveryRequestRepository
                .existsByOrderId(order.getId())) {

            throw new BusinessException(
                    "Delivery request already exists for this order",
                    400
            );
        }

        if ("CANCELLED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            throw new BusinessException(
                    "Cannot create delivery request for cancelled order",
                    400
            );
        }

        if ("DELIVERED".equalsIgnoreCase(
                order.getOrderStatus()
        )) {

            throw new BusinessException(
                    "Order has already been delivered",
                    400
            );
        }

        validateDeliveryMode(
                request.getDeliveryMode()
        );

        validateCoordinates(request);

        DeliveryRequest deliveryRequest =
                new DeliveryRequest();

        deliveryRequest.setOrder(order);

        deliveryRequest.setCustomer(user);

        deliveryRequest.setPickupAddressId(
                request.getPickupAddressId()
        );

        deliveryRequest.setDeliveryAddressId(
                request.getDeliveryAddressId()
        );

        deliveryRequest.setMaxBudget(
                request.getMaxBudget()
        );

        deliveryRequest.setDeliveryMode(
                request.getDeliveryMode()
                        .toUpperCase()
        );

        deliveryRequest.setPickupLatitude(
                request.getPickupLatitude()
        );

        deliveryRequest.setPickupLongitude(
                request.getPickupLongitude()
        );

        deliveryRequest.setDeliveryLatitude(
                request.getDeliveryLatitude()
        );

        deliveryRequest.setDeliveryLongitude(
                request.getDeliveryLongitude()
        );

        deliveryRequest.setRequestedDepartureAt(
                request.getRequestedDepartureAt()
        );

        deliveryRequest.setRequiredByAt(
                request.getRequiredByAt()
        );

        deliveryRequest.setSearchRadiusKm(
                request.getSearchRadiusKm() != null
                        ? request.getSearchRadiusKm()
                        : BigDecimal.TEN
        );

        deliveryRequest.setCustomerNote(
                request.getCustomerNote()
        );

        OffsetDateTime now =
                OffsetDateTime.now();

        deliveryRequest.setRequestedAt(now);

        /*
         * Request remains open for 30 minutes
         * in the initial MVP.
         */
        deliveryRequest.setExpiresAt(
                now.plusMinutes(30)
        );

        deliveryRequest.setStatus("OPEN");

        DeliveryRequest saved =
                deliveryRequestRepository
                        .save(deliveryRequest);

        /*
         * The order is now waiting for delivery
         * assignment.
         */
        order.setOrderStatus(
                "READY_FOR_PICKUP"
        );

        orderRepository.save(order);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryRequestResponse getRequest(
            Long requestId,
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        DeliveryRequest request =
                deliveryRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery request not found",
                                        404
                                )
                        );

        if (!request.getCustomer()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not authorized to view this request",
                    403
            );
        }

        return mapToResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryRequestResponse> getMyRequests(
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        return deliveryRequestRepository
                .findByCustomerIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void cancelRequest(
            Long requestId,
            String authenticatedPhone
    ) {

        User user = getUser(authenticatedPhone);

        DeliveryRequest request =
                deliveryRequestRepository
                        .findById(requestId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Delivery request not found",
                                        404
                                )
                        );

        if (!request.getCustomer()
                .getId()
                .equals(user.getId())) {

            throw new BusinessException(
                    "You are not authorized to cancel this request",
                    403
            );
        }

        if (!"OPEN".equalsIgnoreCase(
                request.getStatus()
        )) {

            throw new BusinessException(
                    "Only open delivery requests can be cancelled",
                    400
            );
        }

        request.setStatus("CANCELLED");

        deliveryRequestRepository.save(request);
    }

    private User getUser(
            String authenticatedPhone
    ) {

        return userRepository
                .findByPhone(authenticatedPhone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );
    }

    private void validateDeliveryMode(
            String mode
    ) {

        if (mode == null ||
                mode.isBlank()) {

            return;
        }

        String normalized =
                mode.toUpperCase();

        if (!normalized.equals("DIRECT") &&
                !normalized.equals("ROUTE_MATCH") &&
                !normalized.equals("SELF_PICKUP")) {

            throw new BusinessException(
                    "Invalid delivery mode",
                    400
            );
        }
    }

    private void validateCoordinates(
            CreateDeliveryRequest request
    ) {

        validateLatitude(
                request.getPickupLatitude()
        );

        validateLongitude(
                request.getPickupLongitude()
        );

        validateLatitude(
                request.getDeliveryLatitude()
        );

        validateLongitude(
                request.getDeliveryLongitude()
        );
    }

    private void validateLatitude(
            BigDecimal latitude
    ) {

        if (latitude == null) {
            return;
        }

        if (latitude.compareTo(
                BigDecimal.valueOf(-90)
        ) < 0 ||
                latitude.compareTo(
                        BigDecimal.valueOf(90)
                ) > 0) {

            throw new BusinessException(
                    "Invalid latitude",
                    400
            );
        }
    }

    private void validateLongitude(
            BigDecimal longitude
    ) {

        if (longitude == null) {
            return;
        }

        if (longitude.compareTo(
                BigDecimal.valueOf(-180)
        ) < 0 ||
                longitude.compareTo(
                        BigDecimal.valueOf(180)
                ) > 0) {

            throw new BusinessException(
                    "Invalid longitude",
                    400
            );
        }
    }

    private DeliveryRequestResponse mapToResponse(
            DeliveryRequest request
    ) {

        DeliveryRequestResponse response =
                new DeliveryRequestResponse();

        response.setId(request.getId());

        response.setOrderId(
                request.getOrder().getId()
        );

        response.setOrderNumber(
                request.getOrder().getOrderNumber()
        );

        response.setCustomerId(
                request.getCustomer().getId()
        );

        response.setPickupAddressId(
                request.getPickupAddressId()
        );

        response.setDeliveryAddressId(
                request.getDeliveryAddressId()
        );

        response.setMaxBudget(
                request.getMaxBudget()
        );

        response.setStatus(
                request.getStatus()
        );

        response.setDeliveryMode(
                request.getDeliveryMode()
        );

        response.setPickupLatitude(
                request.getPickupLatitude()
        );

        response.setPickupLongitude(
                request.getPickupLongitude()
        );

        response.setDeliveryLatitude(
                request.getDeliveryLatitude()
        );

        response.setDeliveryLongitude(
                request.getDeliveryLongitude()
        );

        response.setRequestedDepartureAt(
                request.getRequestedDepartureAt()
        );

        response.setRequiredByAt(
                request.getRequiredByAt()
        );

        response.setSearchRadiusKm(
                request.getSearchRadiusKm()
        );

        response.setCustomerNote(
                request.getCustomerNote()
        );

        response.setRequestedAt(
                request.getRequestedAt()
        );

        response.setExpiresAt(
                request.getExpiresAt()
        );

        response.setCreatedAt(
                request.getCreatedAt()
        );

        response.setUpdatedAt(
                request.getUpdatedAt()
        );

        return response;
    }
}