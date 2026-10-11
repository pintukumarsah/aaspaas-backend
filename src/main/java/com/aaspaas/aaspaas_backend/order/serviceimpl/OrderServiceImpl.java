// package com.aaspaas.aaspaas_backend.order.serviceimpl;

// import com.aaspaas.aaspaas_backend.business.entity.Business;
// import com.aaspaas.aaspaas_backend.cart.entity.Cart;
// import com.aaspaas.aaspaas_backend.cart.entity.CartItem;
// import com.aaspaas.aaspaas_backend.cart.repository.CartRepository;
// import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
// import com.aaspaas.aaspaas_backend.order.dto.CreateOrderRequest;
// import com.aaspaas.aaspaas_backend.order.dto.OrderItemResponse;
// import com.aaspaas.aaspaas_backend.order.dto.OrderResponse;
// import com.aaspaas.aaspaas_backend.order.entity.Order;
// import com.aaspaas.aaspaas_backend.order.entity.OrderItem;
// import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;
// import com.aaspaas.aaspaas_backend.product.entity.Product;
// import com.aaspaas.aaspaas_backend.user.entity.User;
// import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
// import lombok.RequiredArgsConstructor;
// import com.aaspaas.aaspaas_backend.order.service.OrderService;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import com.aaspaas.aaspaas_backend.common.idempotency.IdempotencyConflictException;
// import com.aaspaas.aaspaas_backend.common.idempotency.IdempotencyRecord;
// import com.aaspaas.aaspaas_backend.common.idempotency.IdempotencyRecordRepository;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import jakarta.persistence.EntityManager;
// import jakarta.persistence.PersistenceContext;

// import java.nio.charset.StandardCharsets;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
// import java.util.HexFormat;
// import java.math.BigDecimal;
// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.UUID;

// @Service
// @RequiredArgsConstructor
// @Transactional
// public class OrderServiceImpl implements OrderService {

//     private final OrderRepository orderRepository;
//     private final CartRepository cartRepository;
//     private final UserRepository userRepository;

//     private final IdempotencyRecordRepository idempotencyRecordRepository;
// private final ObjectMapper objectMapper;

// @PersistenceContext
// private EntityManager entityManager;


// @Override
// @Transactional
// public OrderResponse createOrder(
//         CreateOrderRequest request,
//         String authenticatedPhone,
//         String idempotencyKey) {

//     validateIdempotencyKey(idempotencyKey);

//     String endpoint = "POST:/api/orders";
//     String lockKey = authenticatedPhone + "|" + endpoint + "|" + idempotencyKey;

//     /*
//      * PostgreSQL transaction-level advisory lock.
//      * Simultaneous requests with the same key wait for one another.
//      */
//     entityManager.createNativeQuery(
//             "SELECT pg_advisory_xact_lock(hashtextextended(?1, 0))"
//     ).setParameter(1, lockKey).getSingleResult();

//     String requestHash = calculateRequestHash(request);

//     var existing = idempotencyRecordRepository
//             .findByCustomerPhoneAndEndpointAndIdempotencyKey(
//                     authenticatedPhone,
//                     endpoint,
//                     idempotencyKey
//             );

//     if (existing.isPresent()) {
//         IdempotencyRecord record = existing.get();

//         if (!MessageDigest.isEqual(
//                 record.getRequestHash().getBytes(StandardCharsets.UTF_8),
//                 requestHash.getBytes(StandardCharsets.UTF_8))) {

//             throw new IdempotencyConflictException(
//                     "This Idempotency-Key was already used with a different request."
//             );
//         }

//         try {
//             return objectMapper.readValue(
//                     record.getResponseBody(),
//                     OrderResponse.class
//             );
//         } catch (JsonProcessingException ex) {
//             throw new IllegalStateException(
//                     "Stored idempotency response could not be read.", ex
//             );
//         }
//     }

//     /*
//      * Calls the existing order-creation method.
//      * Its order creation and this record share the outer transaction.
//      */
//     OrderResponse response = createOrder(request, authenticatedPhone);

//     try {
//         IdempotencyRecord record = new IdempotencyRecord();
//         record.setCustomerPhone(authenticatedPhone);
//         record.setEndpoint(endpoint);
//         record.setIdempotencyKey(idempotencyKey);
//         record.setRequestHash(requestHash);
//         record.setResponseStatus(201);
//         record.setResponseBody(objectMapper.writeValueAsString(response));

//         idempotencyRecordRepository.saveAndFlush(record);

//         return response;

//     } catch (JsonProcessingException ex) {
//         throw new IllegalStateException(
//                 "Order response could not be stored for idempotency.", ex
//         );
//     }
// }

// private void validateIdempotencyKey(String key) {
//     if (key == null || key.isBlank()) {
//         throw new IdempotencyConflictException(
//                 "Idempotency-Key header is required."
//         );
//     }

//     if (key.length() > 100
//             || !key.matches("[A-Za-z0-9._:-]{8,100}")) {
//         throw new IdempotencyConflictException(
//                 "Idempotency-Key must be 8-100 characters and contain only letters, numbers, ., _, : or -."
//         );
//     }
// }

// private String calculateRequestHash(CreateOrderRequest request) {
//     try {
//         byte[] bytes = objectMapper.writeValueAsBytes(request);
//         byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
//         return HexFormat.of().formatHex(digest);
//     } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
//         throw new IllegalStateException(
//                 "Could not calculate request fingerprint.", ex
//         );
//     }
// }
// //     @Override
// //     public OrderResponse createOrder(
// //             CreateOrderRequest request,
// //             String authenticatedPhone
// //     ) {

// //         User user = getUser(authenticatedPhone);

// //         Cart cart = cartRepository
// //                 .findByUserId(user.getId())
// //                 .orElseThrow(() ->
// //                         new BusinessException(
// //                                 "Cart not found",
// //                                 404
// //                         )
// //                 );

// //         if (cart.getItems() == null ||
// //                 cart.getItems().isEmpty()) {

// //             throw new BusinessException(
// //                     "Cannot create order with empty cart",
// //                     400
// //             );
// //         }

// //         validateStock(cart);

// //         CartItem firstCartItem =
// //                 cart.getItems().get(0);

// //         Business business =
// //                 firstCartItem
// //                         .getProduct()
// //                         .getBusiness();

// //         BigDecimal subtotal =
// //                 calculateSubtotal(cart);

// //         /*
// //          * Temporary MVP values.
// //          *
// //          * Later delivery module will determine
// //          * actual delivery fee.
// //          */
// //         BigDecimal deliveryFee =
// //                 BigDecimal.ZERO;

// //         BigDecimal platformFee =
// //                 BigDecimal.ZERO;

// //         BigDecimal discountAmount =
// //                 BigDecimal.ZERO;

// //         BigDecimal totalAmount =
// //                 subtotal
// //                         .add(deliveryFee)
// //                         .add(platformFee)
// //                         .subtract(discountAmount);

// //         Order order = new Order();

// //         order.setOrderNumber(generateOrderNumber());

// //         order.setCustomer(user);

// //         order.setBusiness(business);

// //         order.setDeliveryAddressId(
// //                 request.getDeliveryAddressId()
// //         );

// //         order.setSubtotal(subtotal);

// //         order.setDeliveryFee(deliveryFee);

// //         order.setPlatformFee(platformFee);

// //         order.setDiscountAmount(discountAmount);

// //         order.setTotalAmount(totalAmount);

// //         order.setPaymentStatus("PENDING");

// //         order.setOrderStatus("PENDING");

// //         /*
// //          * Create order items using snapshots.
// //          */
// //         for (CartItem cartItem : cart.getItems()) {

// //             Product product =
// //                     cartItem.getProduct();

// //             BigDecimal quantity =
// //                     cartItem.getQuantity();

// //             BigDecimal unitPrice =
// //                     product.getPrice();

// //             BigDecimal totalPrice =
// //                     unitPrice.multiply(quantity);

// //             OrderItem orderItem =
// //                     new OrderItem();

// //             orderItem.setOrder(order);

// //             orderItem.setProduct(product);

// //             orderItem.setProductName(
// //                     product.getName()
// //             );

// //             orderItem.setQuantity(quantity);

// //             orderItem.setUnitPrice(unitPrice);

// //             orderItem.setTotalPrice(totalPrice);

// //             order.getItems().add(orderItem);
// //         }

// //         Order savedOrder =
// //                 orderRepository.save(order);

// //         /*
// //          * Clear cart after successful order creation.
// //          */
// //         cart.getItems().clear();

// //         cartRepository.save(cart);

// //         return mapToResponse(savedOrder);
// //     }

//     @Override
//     @Transactional(readOnly = true)
//     public OrderResponse getMyOrder(
//             Long orderId,
//             String authenticatedPhone
//     ) {

//         User user = getUser(authenticatedPhone);

//         Order order =
//                 orderRepository.findById(orderId)
//                         .orElseThrow(() ->
//                                 new BusinessException(
//                                         "Order not found",
//                                         404
//                                 )
//                         );

//         if (!order.getCustomer()
//                 .getId()
//                 .equals(user.getId())) {

//             throw new BusinessException(
//                     "You are not authorized to view this order",
//                     403
//             );
//         }

//         return mapToResponse(order);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<OrderResponse> getMyOrders(
//             String authenticatedPhone
//     ) {

//         User user = getUser(authenticatedPhone);

//         return orderRepository
//                 .findByCustomerIdOrderByCreatedAtDesc(
//                         user.getId()
//                 )
//                 .stream()
//                 .map(this::mapToResponse)
//                 .toList();
//     }

//     private User getUser(
//             String authenticatedPhone
//     ) {

//         return userRepository
//                 .findByPhone(authenticatedPhone)
//                 .orElseThrow(() ->
//                         new BusinessException(
//                                 "User not found",
//                                 404
//                         )
//                 );
//     }

//     private void validateStock(
//             Cart cart
//     ) {

//         for (CartItem cartItem : cart.getItems()) {

//             Product product =
//                     cartItem.getProduct();

//             if (!"ACTIVE".equalsIgnoreCase(
//                     product.getStatus()
//             )) {

//                 throw new BusinessException(
//                         "Product is no longer active: "
//                                 + product.getName(),
//                         400
//                 );
//             }

//             if (!Boolean.TRUE.equals(
//                     product.getAvailable()
//             )) {

//                 throw new BusinessException(
//                         "Product is no longer available: "
//                                 + product.getName(),
//                         400
//                 );
//             }

//             if (cartItem.getQuantity()
//                     .compareTo(
//                             product.getStockQuantity()
//                     ) > 0) {

//                 throw new BusinessException(
//                         "Insufficient stock for product: "
//                                 + product.getName(),
//                         400
//                 );
//             }
//         }
//     }

//     private BigDecimal calculateSubtotal(
//             Cart cart
//     ) {

//         return cart.getItems()
//                 .stream()
//                 .map(item ->
//                         item.getProduct()
//                                 .getPrice()
//                                 .multiply(
//                                         item.getQuantity()
//                                 )
//                 )
//                 .reduce(
//                         BigDecimal.ZERO,
//                         BigDecimal::add
//                 );
//     }

//     private String generateOrderNumber() {

//         String randomPart =
//                 UUID.randomUUID()
//                         .toString()
//                         .replace("-", "")
//                         .substring(0, 10)
//                         .toUpperCase();

//         return "AAP-" + randomPart;
//     }

//     private OrderResponse mapToResponse(
//             Order order
//     ) {

//         OrderResponse response =
//                 new OrderResponse();

//         response.setId(order.getId());

//         response.setOrderNumber(
//                 order.getOrderNumber()
//         );

//         response.setCustomerId(
//                 order.getCustomer().getId()
//         );

//         response.setBusinessId(
//                 order.getBusiness().getId()
//         );

//         response.setBusinessName(
//                 order.getBusiness().getName()
//         );

//         response.setDeliveryAddressId(
//                 order.getDeliveryAddressId()
//         );

//         response.setSubtotal(
//                 order.getSubtotal()
//         );

//         response.setDeliveryFee(
//                 order.getDeliveryFee()
//         );

//         response.setPlatformFee(
//                 order.getPlatformFee()
//         );

//         response.setDiscountAmount(
//                 order.getDiscountAmount()
//         );

//         response.setTotalAmount(
//                 order.getTotalAmount()
//         );

//         response.setPaymentStatus(
//                 order.getPaymentStatus()
//         );

//         response.setOrderStatus(
//                 order.getOrderStatus()
//         );

//         response.setItems(
//                 order.getItems()
//                         .stream()
//                         .map(this::mapItem)
//                         .toList()
//         );

//         response.setCreatedAt(
//                 order.getCreatedAt()
//         );

//         response.setUpdatedAt(
//                 order.getUpdatedAt()
//         );

//         return response;
//     }

//     private OrderItemResponse mapItem(
//             OrderItem item
//     ) {

//         OrderItemResponse response =
//                 new OrderItemResponse();

//         response.setId(item.getId());

//         response.setProductId(
//                 item.getProduct() != null
//                         ? item.getProduct().getId()
//                         : null
//         );

//         response.setProductName(
//                 item.getProductName()
//         );

//         response.setQuantity(
//                 item.getQuantity()
//         );

//         response.setUnitPrice(
//                 item.getUnitPrice()
//         );

//         response.setTotalPrice(
//                 item.getTotalPrice()
//         );

//         return response;
//     }
// }


package com.aaspaas.aaspaas_backend.order.serviceimpl;

import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.cart.entity.Cart;
import com.aaspaas.aaspaas_backend.cart.entity.CartItem;
import com.aaspaas.aaspaas_backend.cart.repository.CartRepository;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.common.idempotency.IdempotencyConflictException;
import com.aaspaas.aaspaas_backend.common.idempotency.IdempotencyRecord;
import com.aaspaas.aaspaas_backend.common.idempotency.IdempotencyRecordRepository;
import com.aaspaas.aaspaas_backend.order.dto.CreateOrderRequest;
import com.aaspaas.aaspaas_backend.order.dto.OrderItemResponse;
import com.aaspaas.aaspaas_backend.order.dto.OrderResponse;
import com.aaspaas.aaspaas_backend.order.entity.Order;
import com.aaspaas.aaspaas_backend.order.entity.OrderItem;
import com.aaspaas.aaspaas_backend.order.repository.OrderRepository;
import com.aaspaas.aaspaas_backend.order.service.OrderService;
import com.aaspaas.aaspaas_backend.product.entity.Product;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public OrderResponse createOrder(
            CreateOrderRequest request,
            String authenticatedPhone,
            String idempotencyKey) {

        validateIdempotencyKey(idempotencyKey);

        String endpoint = "POST:/api/orders";
        String lockKey = authenticatedPhone + "|" + endpoint + "|" + idempotencyKey;

        /*
         * PostgreSQL transaction-level advisory lock.
         * Simultaneous requests with the same key wait for one another.
         */
        entityManager.createNativeQuery(
                "SELECT pg_advisory_xact_lock(hashtextextended(?1, 0))"
        ).setParameter(1, lockKey).getSingleResult();

        String requestHash = calculateRequestHash(request);

        var existing = idempotencyRecordRepository
                .findByCustomerPhoneAndEndpointAndIdempotencyKey(
                        authenticatedPhone,
                        endpoint,
                        idempotencyKey
                );

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();

            if (!MessageDigest.isEqual(
                    record.getRequestHash().getBytes(StandardCharsets.UTF_8),
                    requestHash.getBytes(StandardCharsets.UTF_8))) {

                throw new IdempotencyConflictException(
                        "This Idempotency-Key was already used with a different request."
                );
            }

            try {
                return objectMapper.readValue(
                        record.getResponseBody(),
                        OrderResponse.class
                );
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(
                        "Stored idempotency response could not be read.", ex
                );
            }
        }

        /*
         * Executes order creation and saves the idempotency record
         * within the current database transaction.
         */
        OrderResponse response = executeOrderCreation(request, authenticatedPhone);

        try {
            IdempotencyRecord record = new IdempotencyRecord();
            record.setCustomerPhone(authenticatedPhone);
            record.setEndpoint(endpoint);
            record.setIdempotencyKey(idempotencyKey);
            record.setRequestHash(requestHash);
            record.setResponseStatus(201);
            record.setResponseBody(objectMapper.writeValueAsString(response));

            idempotencyRecordRepository.saveAndFlush(record);

            return response;

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Order response could not be stored for idempotency.", ex
            );
        }
    }

    private OrderResponse executeOrderCreation(
            CreateOrderRequest request,
            String authenticatedPhone
    ) {
        User user = getUser(authenticatedPhone);

        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new BusinessException(
                                "Cart not found",
                                404
                        )
                );

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(
                    "Cannot create order with empty cart",
                    400
            );
        }

        validateStock(cart);

        CartItem firstCartItem = cart.getItems().get(0);
        Business business = firstCartItem.getProduct().getBusiness();

        BigDecimal subtotal = calculateSubtotal(cart);

        /*
         * Temporary MVP values.
         */
        BigDecimal deliveryFee = BigDecimal.ZERO;
        BigDecimal platformFee = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;

        BigDecimal totalAmount = subtotal
                .add(deliveryFee)
                .add(platformFee)
                .subtract(discountAmount);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(user);
        order.setBusiness(business);
        order.setDeliveryAddressId(request.getDeliveryAddressId());
        order.setSubtotal(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setPlatformFee(platformFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("PENDING");
        order.setOrderStatus("PENDING");

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            BigDecimal quantity = cartItem.getQuantity();
            BigDecimal unitPrice = product.getPrice();
            BigDecimal totalPrice = unitPrice.multiply(quantity);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(totalPrice);

            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(
            Long orderId,
            String authenticatedPhone
    ) {
        User user = getUser(authenticatedPhone);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Order not found",
                                404
                        )
                );

        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new BusinessException(
                    "You are not authorized to view this order",
                    403
            );
        }

        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(
            String authenticatedPhone
    ) {
        User user = getUser(authenticatedPhone);

        return orderRepository
                .findByCustomerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private void validateIdempotencyKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key header is required."
            );
        }

        if (key.length() > 100 || !key.matches("[A-Za-z0-9._:-]{8,100}")) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key must be 8-100 characters and contain only letters, numbers, ., _, : or -."
            );
        }
    }

    private String calculateRequestHash(CreateOrderRequest request) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(request);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
            return HexFormat.of().formatHex(digest);
        } catch (JsonProcessingException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "Could not calculate request fingerprint.", ex
            );
        }
    }

    private User getUser(String authenticatedPhone) {
        return userRepository
                .findByPhone(authenticatedPhone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );
    }

    private void validateStock(Cart cart) {
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
                throw new BusinessException(
                        "Product is no longer active: " + product.getName(),
                        400
                );
            }

            if (!Boolean.TRUE.equals(product.getAvailable())) {
                throw new BusinessException(
                        "Product is no longer available: " + product.getName(),
                        400
                );
            }

            if (cartItem.getQuantity().compareTo(product.getStockQuantity()) > 0) {
                throw new BusinessException(
                        "Insufficient stock for product: " + product.getName(),
                        400
                );
            }
        }
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems()
                .stream()
                .map(item -> item.getProduct().getPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNumber() {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();

        return "AAP-" + randomPart;
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerId(order.getCustomer().getId());
        response.setBusinessId(order.getBusiness().getId());
        response.setBusinessName(order.getBusiness().getName());
        response.setDeliveryAddressId(order.getDeliveryAddressId());
        response.setSubtotal(order.getSubtotal());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setPlatformFee(order.getPlatformFee());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setOrderStatus(order.getOrderStatus());
        response.setItems(
                order.getItems()
                        .stream()
                        .map(this::mapItem)
                        .toList()
        );
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        return response;
    }

    private OrderItemResponse mapItem(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(
                item.getProduct() != null ? item.getProduct().getId() : null
        );
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getTotalPrice());

        return response;
    }
}