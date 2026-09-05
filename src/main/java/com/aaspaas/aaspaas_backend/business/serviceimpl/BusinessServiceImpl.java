package com.aaspaas.aaspaas_backend.business.serviceimpl;

import com.aaspaas.aaspaas_backend.business.dto.BusinessResponse;
import com.aaspaas.aaspaas_backend.business.dto.CreateBusinessRequest;
import com.aaspaas.aaspaas_backend.business.entity.Business;
import com.aaspaas.aaspaas_backend.business.repository.BusinessRepository;
import com.aaspaas.aaspaas_backend.business.service.BusinessService;
import com.aaspaas.aaspaas_backend.common.exception.BusinessException;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;

    private final UserRepository userRepository;

    @Override
    @Transactional
    public BusinessResponse createBusiness(
            CreateBusinessRequest request,
            String phone
    ) {

        User owner = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );

        Business business = new Business();

        business.setOwner(owner);
        business.setName(request.getName());
        business.setDescription(request.getDescription());
        business.setBusinessType(request.getBusinessType());
        business.setPhone(request.getPhone());
        business.setAddressId(request.getAddressId());
        business.setLatitude(request.getLatitude());
        business.setLongitude(request.getLongitude());
        business.setStatus("ACTIVE");

        Business savedBusiness =
                businessRepository.save(business);

        return BusinessResponse.fromEntity(savedBusiness);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessResponse> getMyBusinesses(
            String phone
    ) {

        User owner = userRepository.findByPhone(phone)
                .orElseThrow(() ->
                        new BusinessException(
                                "User not found",
                                404
                        )
                );

        return businessRepository
                .findByOwnerId(owner.getId())
                .stream()
                .map(BusinessResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessResponse getBusinessById(
            Long businessId
    ) {

        Business business = businessRepository
                .findById(businessId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Business not found",
                                404
                        )
                );

        return BusinessResponse.fromEntity(business);
    }
}