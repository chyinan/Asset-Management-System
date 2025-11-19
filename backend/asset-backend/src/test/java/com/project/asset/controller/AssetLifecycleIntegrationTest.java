package com.project.asset.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.asset.domain.entity.Asset;
import com.project.asset.domain.entity.AssetType;
import com.project.asset.domain.entity.Department;
import com.project.asset.domain.entity.Permission;
import com.project.asset.domain.entity.Role;
import com.project.asset.domain.entity.User;
import com.project.asset.domain.entity.Vendor;
import com.project.asset.domain.enums.AssetStatus;
import com.project.asset.domain.enums.InventoryStatus;
import com.project.asset.dto.asset.AssetRequestDetailDto;
import com.project.asset.repository.AssetRepository;
import com.project.asset.repository.AssetTypeRepository;
import com.project.asset.repository.DepartmentRepository;
import com.project.asset.repository.PermissionRepository;
import com.project.asset.repository.RoleRepository;
import com.project.asset.repository.UserRepository;
import com.project.asset.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AssetLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AssetTypeRepository assetTypeRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AssetRepository assetRepository;

    private Department department;
    private AssetType laptopType;
    private Vendor vendor;
    private User requester;
    private User approver;
    private Asset templateAsset;

    @BeforeEach
    void initData() {
        Permission perm = new Permission();
        perm.setCode("asset:view");
        perm.setName("View");
        perm = permissionRepository.save(perm);

        Role requesterRole = new Role();
        requesterRole.setCode("ROLE_USER");
        requesterRole.setName("User");
        requesterRole.setPermissions(java.util.Set.of(perm));
        requesterRole = roleRepository.save(requesterRole);

        Role approverRole = new Role();
        approverRole.setCode("ROLE_ADMIN");
        approverRole.setName("Admin");
        approverRole.setPermissions(java.util.Set.of(perm));
        approverRole = roleRepository.save(approverRole);

        department = new Department();
        department.setName("IT");
        department = departmentRepository.save(department);

        laptopType = new AssetType();
        laptopType.setName("Laptop");
        laptopType.setCode("LAP");
        laptopType = assetTypeRepository.save(laptopType);

        vendor = new Vendor();
        vendor.setName("Lenovo");
        vendor = vendorRepository.save(vendor);

        requester = new User();
        requester.setUsername("requester");
        requester.setPassword("pwd");
        requester.setDepartment(department);
        requester.setRoles(java.util.Set.of(requesterRole));
        requester = userRepository.save(requester);

        approver = new User();
        approver.setUsername("approver");
        approver.setPassword("pwd");
        approver.setDepartment(department);
        approver.setRoles(java.util.Set.of(approverRole));
        approver = userRepository.save(approver);

        templateAsset = new Asset();
        templateAsset.setAssetNo("AST-10001");
        templateAsset.setName("ThinkPad X1");
        templateAsset.setAssetType(laptopType);
        templateAsset.setVendor(vendor);
        templateAsset.setStatus(AssetStatus.DRAFT);
        templateAsset.setCreatedBy(requester);
        templateAsset = assetRepository.save(templateAsset);
    }

    @Test
    @WithMockUser(authorities = {
        "asset:apply",
        "asset:view",
        "asset:approve",
        "asset:stockin",
        "asset:checkout",
        "asset:return"
    })
    void fullLifecycle_shouldCompleteHappyPath() throws Exception {
        // 1. Create asset request
        String requestBody = """
                {
                  "requesterId": %d,
                  "departmentId": %d,
                  "remark": "integration test",
                  "items": [
                    {"assetTypeId": %d, "quantity": 1, "purpose": "dev test"}
                  ]
                }
                """.formatted(requester.getId(), department.getId(), laptopType.getId());

        MvcResult requestResult = mockMvc
                .perform(post("/api/asset-requests").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk())
                .andReturn();
        AssetRequestDetailDto requestDto =
                readData(requestResult, AssetRequestDetailDto.class);

        // 2. Approve request
        String approvalBody = """
                {
                  "approverId": %d,
                  "result": "APPROVED",
                  "comment": "ok"
                }
                """.formatted(approver.getId());

        mockMvc.perform(post("/api/asset-requests/%d/approve".formatted(requestDto.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(approvalBody))
                .andExpect(status().isOk());

        // 3. Stock in
        String stockInBody = """
                {
                  "assetId": %d,
                  "serialNo": "SN-INT-001",
                  "location": "仓库Z"
                }
                """.formatted(templateAsset.getId());

        MvcResult stockResult = mockMvc
                .perform(post("/api/inventory/stock-in").contentType(MediaType.APPLICATION_JSON).content(stockInBody))
                .andExpect(status().isOk())
                .andReturn();
        Long inventoryId = readData(stockResult, com.project.asset.domain.entity.Inventory.class).getId();

        // 4. Checkout
        String checkoutBody = """
                {
                  "userId": %d,
                  "remark": "project use"
                }
                """.formatted(requester.getId());
        mockMvc.perform(post("/api/inventory/%d/checkout".formatted(inventoryId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkoutBody))
                .andExpect(status().isOk());

        // 5. Return
        String returnBody = """
                {
                  "userId": %d,
                  "remark": "return asset"
                }
                """.formatted(requester.getId());
        MvcResult returnResult = mockMvc
                .perform(post("/api/inventory/%d/return".formatted(inventoryId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(returnBody))
                .andExpect(status().isOk())
                .andReturn();

        com.project.asset.domain.entity.Inventory returned =
                readData(returnResult, com.project.asset.domain.entity.Inventory.class);
        assertThat(returned.getStatus()).isEqualTo(InventoryStatus.IN_STOCK);
    }

    private <T> T readData(MvcResult result, Class<T> clazz) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode dataNode = root.get("data");
        return objectMapper.treeToValue(dataNode, clazz);
    }
}

