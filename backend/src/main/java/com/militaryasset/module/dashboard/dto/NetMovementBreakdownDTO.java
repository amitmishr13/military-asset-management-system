package com.militaryasset.module.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

public class NetMovementBreakdownDTO {

    private LocalDate selectedDate;
    private Long baseId;
    private String baseName;
    private Long equipmentTypeId;
    private String equipmentName;

    private Integer totalPurchases;
    private Integer totalTransferIn;
    private Integer totalTransferOut;
    private Integer netMovement;

    private List<NetMovementDetailItemDTO> items;

    public NetMovementBreakdownDTO() {}

    public NetMovementBreakdownDTO(LocalDate selectedDate, Long baseId, String baseName, Long equipmentTypeId, String equipmentName, Integer totalPurchases, Integer totalTransferIn, Integer totalTransferOut, Integer netMovement, List<NetMovementDetailItemDTO> items) {
        this.selectedDate = selectedDate;
        this.baseId = baseId;
        this.baseName = baseName;
        this.equipmentTypeId = equipmentTypeId;
        this.equipmentName = equipmentName;
        this.totalPurchases = totalPurchases;
        this.totalTransferIn = totalTransferIn;
        this.totalTransferOut = totalTransferOut;
        this.netMovement = netMovement;
        this.items = items;
    }

    public LocalDate getSelectedDate() { return selectedDate; }
    public Long getBaseId() { return baseId; }
    public String getBaseName() { return baseName; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentName() { return equipmentName; }
    public Integer getTotalPurchases() { return totalPurchases; }
    public Integer getTotalTransferIn() { return totalTransferIn; }
    public Integer getTotalTransferOut() { return totalTransferOut; }
    public Integer getNetMovement() { return netMovement; }
    public List<NetMovementDetailItemDTO> getItems() { return items; }
}
