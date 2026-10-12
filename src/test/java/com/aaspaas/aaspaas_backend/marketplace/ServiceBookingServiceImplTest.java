package com.aaspaas.aaspaas_backend.marketplace;

import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.marketplace.dto.BookingResponse;
import com.aaspaas.aaspaas_backend.marketplace.dto.CreateBookingRequest;
import com.aaspaas.aaspaas_backend.marketplace.entity.ServiceBooking;
import com.aaspaas.aaspaas_backend.marketplace.entity.ServiceListing;
import com.aaspaas.aaspaas_backend.marketplace.enums.BookingStatus;
import com.aaspaas.aaspaas_backend.marketplace.enums.ListingStatus;
import com.aaspaas.aaspaas_backend.marketplace.enums.PricingType;
import com.aaspaas.aaspaas_backend.marketplace.repository.ServiceBookingRepository;
import com.aaspaas.aaspaas_backend.marketplace.repository.ServiceListingRepository;
import com.aaspaas.aaspaas_backend.marketplace.serviceimpl.ServiceBookingServiceImpl;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceBookingServiceImplTest {

    @Mock ServiceBookingRepository bookingRepository;
    @Mock ServiceListingRepository listingRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;

    @InjectMocks ServiceBookingServiceImpl service;

    private User customer;
    private User provider;
    private ServiceListing listing;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "commissionPercent", new BigDecimal("10"));
        ReflectionTestUtils.setField(service, "minLeadMinutes", 30L);

        customer = new User();
        customer.setId(1L);
        customer.setFullName("Customer");
        provider = new User();
        provider.setId(2L);
        provider.setFullName("Provider");

        listing = new ServiceListing();
        listing.setId(10L);
        listing.setProvider(provider);
        listing.setTitle("AC repair");
        listing.setPricingType(PricingType.HOURLY);
        listing.setBasePrice(new BigDecimal("500.00"));
        listing.setStatus(ListingStatus.ACTIVE);

        when(userRepository.findByPhone("9999999999")).thenReturn(Optional.of(customer));
        when(listingRepository.findById(10L)).thenReturn(Optional.of(listing));
        when(bookingRepository.existsByCustomerIdAndListingIdAndScheduledAtAndStatusIn(
                any(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any(ServiceBooking.class))).thenAnswer(inv -> {
            ServiceBooking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });
    }

    private CreateBookingRequest request(Integer hours, OffsetDateTime when) {
        CreateBookingRequest r = new CreateBookingRequest();
        r.setListingId(10L);
        r.setScheduledAt(when);
        r.setAddressText("House 12, Main Road, Jaipur");
        r.setEstimatedHours(hours);
        return r;
    }

    @Test
    void hourlyBookingComputesTotalCommissionAndEarning() {
        BookingResponse res = service.create(request(2, OffsetDateTime.now().plusDays(1)), "9999999999");

        assertEquals(new BigDecimal("1000.00"), res.getTotalAmount());
        assertEquals(new BigDecimal("100.00"), res.getCommissionAmount());
        assertEquals(new BigDecimal("900.00"), res.getProviderEarning());
        assertEquals(BookingStatus.REQUESTED, res.getStatus());
    }

    @Test
    void fixedBookingIgnoresHours() {
        listing.setPricingType(PricingType.FIXED);
        BookingResponse res = service.create(request(null, OffsetDateTime.now().plusDays(1)), "9999999999");
        assertEquals(new BigDecimal("500.00"), res.getTotalAmount());
        assertNull(res.getEstimatedHours());
    }

    @Test
    void hourlyBookingWithoutHoursIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.create(request(null, OffsetDateTime.now().plusDays(1)), "9999999999"));
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void cannotBookOwnService() {
        listing.setProvider(customer);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.create(request(2, OffsetDateTime.now().plusDays(1)), "9999999999"));
        assertEquals(409, ex.getStatusCode());
    }

    @Test
    void pastOrTooSoonTimeIsRejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.create(request(2, OffsetDateTime.now().plusMinutes(5)), "9999999999"));
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void inactiveListingCannotBeBooked() {
        listing.setStatus(ListingStatus.PAUSED);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.create(request(2, OffsetDateTime.now().plusDays(1)), "9999999999"));
        assertEquals(409, ex.getStatusCode());
    }
}
