package com.aaspaas.aaspaas_backend.marketplace.service;

import com.aaspaas.aaspaas_backend.marketplace.dto.BookingResponse;
import com.aaspaas.aaspaas_backend.marketplace.dto.CreateBookingRequest;
import com.aaspaas.aaspaas_backend.marketplace.enums.BookingStatus;
import org.springframework.data.domain.Page;

public interface ServiceBookingService {

    // customer
    BookingResponse create(CreateBookingRequest request, String phone);

    Page<BookingResponse> myBookings(String phone, int page, int size);

    BookingResponse getMine(Long id, String phone);

    BookingResponse cancelByCustomer(Long id, String reason, String phone);

    // provider
    Page<BookingResponse> providerBookings(String phone, BookingStatus status, int page, int size);

    BookingResponse accept(Long id, String phone);

    BookingResponse reject(Long id, String reason, String phone);

    BookingResponse start(Long id, String phone);

    BookingResponse complete(Long id, String phone);

    BookingResponse cancelByProvider(Long id, String reason, String phone);
}
