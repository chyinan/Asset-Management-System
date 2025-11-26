package com.project.asset.controller;

import com.project.asset.dto.system.LoanReminderSettingDto;
import com.project.asset.dto.system.UpdateLoanReminderSettingRequest;
import com.project.asset.response.ApiResponse;
import com.project.asset.security.UserPrincipal;
import com.project.asset.service.LoanReminderService;
import com.project.asset.service.LoanReminderSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System Settings", description = "系统提醒配置")
@RestController
@RequestMapping("/api/system/reminder-settings")
@RequiredArgsConstructor
public class LoanReminderSettingController {

    private final LoanReminderSettingService settingService;
    private final LoanReminderService loanReminderService;

    @Operation(summary = "获取资产归还提醒发送邮箱")
    @GetMapping
    @PreAuthorize("hasAuthority('asset:admin')")
    public ApiResponse<LoanReminderSettingDto> getSettings() {
        return ApiResponse.success(settingService.getSettings());
    }

    @Operation(summary = "更新资产归还提醒发送邮箱")
    @PutMapping
    @PreAuthorize("hasAuthority('asset:admin')")
    public ApiResponse<LoanReminderSettingDto> updateSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateLoanReminderSettingRequest request) {
        Long operatorId = principal != null ? principal.getUserId() : null;
        LoanReminderSettingDto updated = settingService.updateSettings(request, operatorId);
        loanReminderService.refreshSchedule();
        return ApiResponse.success(updated);
    }
}
