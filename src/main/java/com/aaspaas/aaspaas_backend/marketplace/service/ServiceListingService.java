package com.aaspaas.aaspaas_backend.marketplace.service;

import com.aaspaas.aaspaas_backend.marketplace.dto.ListingRequest;
import com.aaspaas.aaspaas_backend.marketplace.dto.ListingResponse;
import com.aaspaas.aaspaas_backend.marketplace.enums.ListingStatus;
import org.springframework.data.domain.Page;

public interface ServiceListingService {

    // provider
    ListingResponse create(ListingRequest request, String phone);

    ListingResponse update(Long id, ListingRequest request, String phone);

    ListingResponse pause(Long id, String phone);

    ListingResponse resume(Long id, String phone);

    Page<ListingResponse> myListings(String phone, int page, int size);

    // public
    Page<ListingResponse> search(Long categoryId, String city, String query, int page, int size);

    ListingResponse getPublic(Long id);

    // admin
    Page<ListingResponse> adminList(ListingStatus status, int page, int size);

    ListingResponse approve(Long id);

    ListingResponse suspend(Long id);

    ListingResponse reactivate(Long id);
}
