package com.project.asset.dto.asset;

import com.project.asset.domain.enums.InventoryStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class InventoryDto {
    Long id;
    Long assetId;
    String assetName;
    String assetNo;
    InventoryStatus status;
    String serialNo;
    String location;
    LocalDateTime updatedAt;
    Long currentHolderId;
    String currentHolderName;
    LocalDateTime checkedOutAt;
    LocalDateTime expectedReturnAt;
    LocalDateTime lastReminderAt;
    Integer reminderCount;
}

