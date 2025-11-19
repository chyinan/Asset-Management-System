package com.project.asset.controller;

import com.project.asset.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "服务健康检查")
    @GetMapping
    public ApiResponse<String> health() {
        return ApiResponse.success("OK");
    }
}


