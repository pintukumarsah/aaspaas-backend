package com.aaspaas.aaspaas_backend.marketplace.serviceimpl;

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
import com.aaspaas.aaspaas_backend.marketplace.service.ServiceBookingService;
import com.aaspaas.aaspaas_backend.notification.service.NotificationService;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Booking lifecycle: REQUESTED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
 *                    REQUESTED -> REJECTED | CANCELLED,  ACCEPTED -> CANCELLED
 *
 * Money: the platform commission is SNAPSHOT on the booking at request time.
 * Collecting the customer's payment is not part of this module (see README).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceBookingServiceImpl implements ServiceBookingService {

    private static final EnumSet<BookingStatus> ACTIVE =
            EnumSet.of(BookingStatus.REQUESTED, BookingStatus.ACCEPTED, BookingStatus.IN_PROGRESS);

    private final ServiceBookingRepository bookingRepository;
    private final ServiceListingRepository listingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${marketplace.commission-percent:10}")
    private BigDecimal commissionPercent;

    @Value("${marketplace.min-lead-minutes:30}")
    private long minLeadMinutes;

    // ------------------------------------------------------------------ customer

    @Override
    @Transactional
    public BookingResponse create(CreateBookingRequest r, String phone) {
        User customer = user(phone);

        ServiceListing listing = listingRepository.findById(r.getListingId())
                .orElseThrow(() -> new BusinessException("Service not found", 404));
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException("This service is not available for booking", 409);
        }
        if (listing.getProvider().getId().equals(customer.getId())) {
            throw new BusinessException("You cannot book your own service", 409);
        }
        if (r.getScheduledAt().isBefore(OffsetDateTime.now().plusMinutes(minLeadMinutes))) {
            throw new BusinessException(
                    "Scheduled time must be at least " + minLeadMinutes + " minutes from now", 400);
        }
        if (bookingRepository.existsByCustomerIdAndListingIdAndScheduledAtAndStatusIn(
                customer.getId(), listing.getId(), r.getScheduledAt(), ACTIVE)) {
            throw new BusinessException("You already have a booking for this service at this time", 409);
        }

        BigDecimal unit = listing.getBasePrice();
        Integer hours = null;
        BigDecimal total;
        if (listing.getPricingType() == PricingType.HOURLY) {
            if (r.getEstimatedHours() == null) {
                throw new BusinessException("estimatedHours is required for an hourly service", 400);
            }
            hours = r.getEstimatedHours();
            total = unit.multiply(BigDecimal.valueOf(hours));
        } else {
            total = unit;
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
        BigDecimal commission = total.multiply(commissionPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        ServiceBooking b = new ServiceBooking();
        b.setBookingNumber(newBookingNumber());
        b.setListing(listing);
        b.setCustomer(customer);
        b.setProvider(listing.getProvider());
        b.setScheduledAt(r.getScheduledAt());
        b.setAddressText(r.getAddressText().trim());
        b.setNotes(r.getNotes() == null ? null : r.getNotes().trim());
        b.setPricingType(listing.getPricingType());
        b.setUnitPrice(unit);
        b.setEstimatedHours(hours);
        b.setTotalAmount(total);
        b.setCommissionPercent(commissionPercent);
        b.setCommissionAmount(commission);
        b.setProviderEarning(total.subtract(commission));
        b.setStatus(BookingStatus.REQUESTED);
        ServiceBooking saved = bookingRepository.save(b);

        notify(listing.getProvider(), "New service booking",
                "You have a new booking request " + saved.getBookingNumber() + ".", saved);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> myBookings(String phone, int page, int size) {
        User c = user(phone);
        return bookingRepository
                .findByCustomerIdOrderByCreatedAtDesc(c.getId(), pageable(page, size))
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getMine(Long id, String phone) {
        User u = user(phone);
        ServiceBooking b = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Booking not found", 404));
        boolean mine = b.getCustomer().getId().equals(u.getId())
                || b.getProvider().getId().equals(u.getId());
        if (!mine) {
            throw new BusinessException("Booking not found", 404);
        }
        return toResponse(b);
    }

    @Override
    @Transactional
    public BookingResponse cancelByCustomer(Long id, String reason, String phone) {
        User c = user(phone);
        ServiceBooking b = locked(id);
        if (!b.getCustomer().getId().equals(c.getId())) {
            throw new BusinessException("Booking not found", 404);
        }
        if (b.getStatus() != BookingStatus.REQUESTED && b.getStatus() != BookingStatus.ACCEPTED) {
            throw new BusinessException("This booking can no longer be cancelled", 409);
        }
        b.setStatus(BookingStatus.CANCELLED);
        b.setStatusReason(clip(reason));
        ServiceBooking saved = bookingRepository.save(b);
        notify(b.getProvider(), "Booking cancelled",
                "Booking " + b.getBookingNumber() + " was cancelled by the customer.", saved);
        return toResponse(saved);
    }

    // ------------------------------------------------------------------ provider

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> providerBookings(
            String phone, BookingStatus status, int page, int size) {
        User p = user(phone);
        Page<ServiceBooking> result = status == null
                ? bookingRepository.findByProviderIdOrderByCreatedAtDesc(p.getId(), pageable(page, size))
                : bookingRepository.findByProviderIdAndStatusOrderByCreatedAtDesc(
                        p.getId(), status, pageable(page, size));
        return result.map(this::toResponse);
    }

    @Override
    @Transactional
    public BookingResponse accept(Long id, String phone) {
        ServiceBooking b = ownedByProvider(id, phone);
        requireStatus(b, BookingStatus.REQUESTED, "Only a REQUESTED booking can be accepted");
        b.setStatus(BookingStatus.ACCEPTED);
        b.setAcceptedAt(OffsetDateTime.now());
        ServiceBooking saved = bookingRepository.save(b);
        notify(b.getCustomer(), "Booking accepted",
                "Your booking " + b.getBookingNumber() + " was accepted.", saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse reject(Long id, String reason, String phone) {
        ServiceBooking b = ownedByProvider(id, phone);
        requireStatus(b, BookingStatus.REQUESTED, "Only a REQUESTED booking can be rejected");
        b.setStatus(BookingStatus.REJECTED);
        b.setStatusReason(clip(reason));
        ServiceBooking saved = bookingRepository.save(b);
        notify(b.getCustomer(), "Booking rejected",
                "Your booking " + b.getBookingNumber() + " could not be accepted.", saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse start(Long id, String phone) {
        ServiceBooking b = ownedByProvider(id, phone);
        requireStatus(b, BookingStatus.ACCEPTED, "Only an ACCEPTED booking can be started");
        b.setStatus(BookingStatus.IN_PROGRESS);
        b.setStartedAt(OffsetDateTime.now());
        ServiceBooking saved = bookingRepository.save(b);
        notify(b.getCustomer(), "Service started",
                "Work on booking " + b.getBookingNumber() + " has started.", saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse complete(Long id, String phone) {
        ServiceBooking b = ownedByProvider(id, phone);
        requireStatus(b, BookingStatus.IN_PROGRESS, "Only an IN_PROGRESS booking can be completed");
        b.setStatus(BookingStatus.COMPLETED);
        b.setCompletedAt(OffsetDateTime.now());
        ServiceBooking saved = bookingRepository.save(b);
        notify(b.getCustomer(), "Service completed",
                "Booking " + b.getBookingNumber() + " is completed.", saved);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse cancelByProvider(Long id, String reason, String phone) {
        ServiceBooking b = ownedByProvider(id, phone);
        requireStatus(b, BookingStatus.ACCEPTED, "Only an ACCEPTED booking can be cancelled by the provider");
        b.setStatus(BookingStatus.CANCELLED);
        b.setStatusReason(clip(reason));
        ServiceBooking saved = bookingRepository.save(b);
        notify(b.getCustomer(), "Booking cancelled",
                "Booking " + b.getBookingNumber() + " was cancelled by the provider.", saved);
        return toResponse(saved);
    }

    // ------------------------------------------------------------------ helpers

    private ServiceBooking ownedByProvider(Long id, String phone) {
        User p = user(phone);
        ServiceBooking b = locked(id);
        if (!b.getProvider().getId().equals(p.getId())) {
            throw new BusinessException("Booking not found", 404);
        }
        return b;
    }

    private ServiceBooking locked(Long id) {
        return bookingRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException("Booking not found", 404));
    }

    private void requireStatus(ServiceBooking b, BookingStatus expected, String message) {
        if (b.getStatus() != expected) {
            throw new BusinessException(message, 409);
        }
    }

    private User user(String phone) {
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException("User not found", 404));
    }

    private void notify(User target, String title, String message, ServiceBooking b) {
        try {
            notificationService.createNotification(
                    target.getId(), title, message, "SERVICE_BOOKING", "SERVICE_BOOKING", b.getId());
        } catch (RuntimeException ex) {
            log.warn("Could not notify user {} for booking {}", target.getId(), b.getId(), ex);
        }
    }

    private String newBookingNumber() {
        return "SB-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    private String clip(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.length() > 500 ? t.substring(0, 500) : t;
    }

    private PageRequest pageable(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Page cannot be negative", 400);
        }
        if (size < 1 || size > 100) {
            throw new BusinessException("Page size must be between 1 and 100", 400);
        }
        return PageRequest.of(page, size);
    }

    private BookingResponse toResponse(ServiceBooking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .bookingNumber(b.getBookingNumber())
                .listingId(b.getListing().getId())
                .listingTitle(b.getListing().getTitle())
                .customerId(b.getCustomer().getId())
                .customerName(b.getCustomer().getFullName())
                .providerId(b.getProvider().getId())
                .providerName(b.getProvider().getFullName())
                .scheduledAt(b.getScheduledAt())
                .addressText(b.getAddressText())
                .notes(b.getNotes())
                .pricingType(b.getPricingType())
                .unitPrice(b.getUnitPrice())
                .estimatedHours(b.getEstimatedHours())
                .totalAmount(b.getTotalAmount())
                .commissionPercent(b.getCommissionPercent())
                .commissionAmount(b.getCommissionAmount())
                .providerEarning(b.getProviderEarning())
                .status(b.getStatus())
                .statusReason(b.getStatusReason())
                .acceptedAt(b.getAcceptedAt())
                .startedAt(b.getStartedAt())
                .completedAt(b.getCompletedAt())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
