package com.project.asset.service;

import com.project.asset.domain.entity.Approval;
import com.project.asset.domain.entity.AssetRequest;
import com.project.asset.domain.entity.AssetType;
import com.project.asset.domain.entity.Department;
import com.project.asset.domain.entity.RequestItem;
import com.project.asset.domain.entity.User;
import com.project.asset.domain.enums.ApprovalResult;
import com.project.asset.domain.enums.RequestStatus;
import com.project.asset.dto.asset.ApprovalRequestDto;
import com.project.asset.dto.asset.AssetRequestCreateDto;
import com.project.asset.dto.asset.AssetRequestDetailDto;
import com.project.asset.dto.asset.RequestItemDto;
import com.project.asset.exception.BusinessException;
import com.project.asset.exception.ErrorCode;
import com.project.asset.repository.ApprovalRepository;
import com.project.asset.repository.AssetRequestRepository;
import com.project.asset.repository.AssetTypeRepository;
import com.project.asset.repository.DepartmentRepository;
import com.project.asset.repository.UserRepository;
import com.project.asset.response.PageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetRequestService {

    private final AssetRequestRepository assetRequestRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AssetTypeRepository assetTypeRepository;
    private final ApprovalRepository approvalRepository;
    private final AuditService auditService;

    public PageResponse<AssetRequestDetailDto> list(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<AssetRequestDetailDto> dtoPage = assetRequestRepository.findAll(pageable).map(this::toDetail);
        return PageResponse.<AssetRequestDetailDto>builder()
                .content(dtoPage.getContent())
                .totalElements(dtoPage.getTotalElements())
                .totalPages(dtoPage.getTotalPages())
                .page(dtoPage.getNumber())
                .size(dtoPage.getSize())
                .build();
    }

    @Transactional
    public AssetRequestDetailDto createRequest(AssetRequestCreateDto dto) {
        User requester = userRepository
                .findById(dto.getRequesterId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申请人不存在"));
        Department department = null;
        if (dto.getDepartmentId() != null) {
            department = departmentRepository
                    .findById(dto.getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "部门不存在"));
        }

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "申请明细不能为空");
        }

        AssetRequest request = new AssetRequest();
        request.setRequestNo(generateRequestNo());
        request.setRequester(requester);
        request.setDepartment(department);
        request.setRemark(dto.getRemark());
        request.setStatus(RequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        List<RequestItem> items = new ArrayList<>();
        for (RequestItemDto itemDto : dto.getItems()) {
            AssetType type = assetTypeRepository
                    .findById(itemDto.getAssetTypeId())
                    .orElseThrow(
                            () -> new BusinessException(ErrorCode.NOT_FOUND, "资产类型不存在: " + itemDto.getAssetTypeId()));
            RequestItem item = new RequestItem();
            item.setRequest(request);
            item.setAssetType(type);
            item.setQuantity(itemDto.getQuantity());
            item.setPurpose(itemDto.getPurpose());
            items.add(item);
        }
        request.setItems(items);
        AssetRequest saved = assetRequestRepository.save(request);
        auditService.record(requester.getId(), "REQUEST_CREATE", "AssetRequest", saved.getId(), dto.getRemark());
        return toDetail(saved);
    }

    public AssetRequestDetailDto getDetail(Long id) {
        AssetRequest request = assetRequestRepository
                .findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申请不存在"));
        return toDetail(request);
    }

    @Transactional
    public AssetRequestDetailDto approve(Long requestId, ApprovalRequestDto dto) {
        AssetRequest request = assetRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "申请不存在"));
        User approver = userRepository
                .findById(dto.getApproverId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "审批人不存在"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.CONFLICT, "该申请已处理");
        }

        request.setStatus(dto.getResult() == ApprovalResult.APPROVED ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        request.setUpdatedAt(LocalDateTime.now());
        assetRequestRepository.save(request);

        Approval approval = new Approval();
        approval.setEntityType("REQUEST");
        approval.setEntityId(request.getId());
        approval.setApprover(approver);
        approval.setResult(dto.getResult());
        approval.setComment(dto.getComment());
        approval.setCreatedAt(LocalDateTime.now());
        approvalRepository.save(approval);

        auditService.record(
                approver.getId(), "REQUEST_APPROVAL", "AssetRequest", request.getId(), dto.getResult().name());
        return toDetail(request);
    }

    private AssetRequestDetailDto toDetail(AssetRequest request) {
        return AssetRequestDetailDto.builder()
                .id(request.getId())
                .requestNo(request.getRequestNo())
                .requesterId(request.getRequester().getId())
                .departmentId(request.getDepartment() != null ? request.getDepartment().getId() : null)
                .status(request.getStatus())
                .remark(request.getRemark())
                .createdAt(request.getCreatedAt())
                .items(request.getItems().stream()
                        .map(item -> RequestItemDto.builder()
                                .id(item.getId())
                                .assetTypeId(item.getAssetType() != null ? item.getAssetType().getId() : null)
                                .assetTypeName(item.getAssetType() != null ? item.getAssetType().getName() : null)
                                .quantity(item.getQuantity())
                                .purpose(item.getPurpose())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private String generateRequestNo() {
        return "REQ-" + System.currentTimeMillis();
    }
}

