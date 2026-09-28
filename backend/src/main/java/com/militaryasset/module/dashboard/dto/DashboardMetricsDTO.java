package com.militaryasset.module.dashboard.dto;

import java.time.LocalDate;

public class DashboardMetricsDTO {

    private LocalDate selectedDate;
    private Long baseId;
    private String baseName;
    private Long equipmentTypeId;
    private String equipmentName;

    private Integer openingBalance;
    private Integer purchases;
    private Integer transferIn;
    private Integer transferOut;
    private Integer netMovement;
    private Integer expendedAssets;
    private Integer closingBalance;
    private Integer activeAssignedAssets;
    private Integer availableStock;

    public DashboardMetricsDTO() {}

    public DashboardMetricsDTO(LocalDate selectedDate, Long baseId, String baseName, Long equipmentTypeId, String equipmentName, Integer openingBalance, Integer purchases, Integer transferIn, Integer transferOut, Integer netMovement, Integer expendedAssets, Integer closingBalance, Integer activeAssignedAssets, Integer availableStock) {
        this.selectedDate = selectedDate;
        this.baseId = baseId;
        this.baseName = baseName;
        this.equipmentTypeId = equipmentTypeId;
        this.equipmentName = equipmentName;
        this.openingBalance = openingBalance;
        this.purchases = purchases;
        this.transferIn = transferIn;
        this.transferOut = transferOut;
        this.netMovement = netMovement;
        this.expendedAssets = expendedAssets;
        this.closingBalance = closingBalance;
        this.activeAssignedAssets = activeAssignedAssets;
        this.availableStock = availableStock;
    }

    public LocalDate getSelectedDate() { return selectedDate; }
    public Long getBaseId() { return baseId; }
    public String getBaseName() { return baseName; }
    public Long getEquipmentTypeId() { return equipmentTypeId; }
    public String getEquipmentName() { return equipmentName; }

    public Integer getOpeningBalance() { return openingBalance; }
    public Integer getPurchases() { return purchases; }
    public Integer getTransferIn() { return transferIn; }
    public Integer getTransferOut() { return transferOut; }
    public Integer getNetMovement() { return netMovement; }
    public Integer getExpendedAssets() { return expendedAssets; }
    public Integer getClosingBalance() { return closingBalance; }
    public Integer getActiveAssignedAssets() { return activeAssignedAssets; }
    public Integer getAvailableStock() { return availableStock; }
}
