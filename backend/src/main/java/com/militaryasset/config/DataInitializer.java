package com.militaryasset.config;

import com.militaryasset.module.assignment.entity.Assignment;
import com.militaryasset.module.assignment.entity.AssignmentStatus;
import com.militaryasset.module.assignment.repository.AssignmentRepository;
import com.militaryasset.module.audit.entity.AuditLog;
import com.militaryasset.module.audit.repository.AuditLogRepository;
import com.militaryasset.module.auth.entity.Role;
import com.militaryasset.module.auth.entity.User;
import com.militaryasset.module.auth.repository.UserRepository;
import com.militaryasset.module.base.entity.Base;
import com.militaryasset.module.base.repository.BaseRepository;
import com.militaryasset.module.equipment.entity.EquipmentType;
import com.militaryasset.module.equipment.entity.InitialInventory;
import com.militaryasset.module.equipment.repository.EquipmentTypeRepository;
import com.militaryasset.module.equipment.repository.InitialInventoryRepository;
import com.militaryasset.module.expenditure.entity.Expenditure;
import com.militaryasset.module.expenditure.repository.ExpenditureRepository;
import com.militaryasset.module.purchase.entity.Purchase;
import com.militaryasset.module.purchase.repository.PurchaseRepository;
import com.militaryasset.module.transfer.entity.Transfer;
import com.militaryasset.module.transfer.repository.TransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private BaseRepository baseRepository;

    @Autowired
    private EquipmentTypeRepository equipmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InitialInventoryRepository initialInventoryRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private ExpenditureRepository expenditureRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (baseRepository.count() > 0) {
            return; // Data already seeded
        }

        // 1. Seed Bases
        Base alpha = baseRepository.save(Base.builder()
                .code("BASE_ALPHA")
                .name("Fort Alpha Command")
                .location("Sector 4 North")
                .build());

        Base bravo = baseRepository.save(Base.builder()
                .code("BASE_BRAVO")
                .name("Bravo Outpost")
                .location("Highland Region")
                .build());

        Base charlie = baseRepository.save(Base.builder()
                .code("BASE_CHARLIE")
                .name("Charlie Garrison")
                .location("Coastal Defense Perimeter")
                .build());

        // 2. Seed Equipment Types
        EquipmentType jeep = equipmentTypeRepository.save(EquipmentType.builder()
                .code("VEHICLE_JEEP")
                .name("Tactical Light Vehicle")
                .category("VEHICLE")
                .unitOfMeasure("UNITS")
                .description("4x4 All-Terrain Utility Transport")
                .build());

        EquipmentType truck = equipmentTypeRepository.save(EquipmentType.builder()
                .code("VEHICLE_TRUCK")
                .name("Heavy Transport Truck")
                .category("VEHICLE")
                .unitOfMeasure("UNITS")
                .description("6x6 Heavy Logistics Carrier")
                .build());

        EquipmentType rifle = equipmentTypeRepository.save(EquipmentType.builder()
                .code("RIFLE_M4")
                .name("M4A1 Assault Rifle")
                .category("WEAPON")
                .unitOfMeasure("UNITS")
                .description("5.56mm Select-Fire Carbine")
                .build());

        EquipmentType ammo556 = equipmentTypeRepository.save(EquipmentType.builder()
                .code("AMMO_556")
                .name("5.56mm NATO Ammunition")
                .category("AMMUNITION")
                .unitOfMeasure("ROUNDS")
                .description("Standard Infantry Ammunition")
                .build());

        EquipmentType ammo155 = equipmentTypeRepository.save(EquipmentType.builder()
                .code("ARTILLERY_155")
                .name("155mm Howitzer Shell")
                .category("AMMUNITION")
                .unitOfMeasure("ROUNDS")
                .description("High Explosive Heavy Ordnance")
                .build());

        // 3. Seed Users (BCrypt Encoded Passwords)
        String defaultPasswordHash = passwordEncoder.encode("Password123!");

        User adminUser = userRepository.save(User.builder()
                .username("admin")
                .passwordHash(defaultPasswordHash)
                .fullName("Gen. Arthur Vance")
                .role(Role.ADMIN)
                .base(null)
                .build());

        User commanderAlpha = userRepository.save(User.builder()
                .username("commander_alpha")
                .passwordHash(defaultPasswordHash)
                .fullName("Col. Marcus Wright")
                .role(Role.BASE_COMMANDER)
                .base(alpha)
                .build());

        User commanderBravo = userRepository.save(User.builder()
                .username("commander_bravo")
                .passwordHash(defaultPasswordHash)
                .fullName("Lt. Col. Elena Rostova")
                .role(Role.BASE_COMMANDER)
                .base(bravo)
                .build());

        User logisticsOfficer = userRepository.save(User.builder()
                .username("logistics_officer")
                .passwordHash(defaultPasswordHash)
                .fullName("Maj. David Sterling")
                .role(Role.LOGISTICS_OFFICER)
                .base(null)
                .build());

        // 4. Seed Initial Inventory (Baseline as of 2026-01-01)
        LocalDate baselineDate = LocalDate.of(2026, 1, 1);

        // Alpha Base Initial Stock
        initialInventoryRepository.save(InitialInventory.builder().base(alpha).equipmentType(jeep).initialQuantity(50).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(alpha).equipmentType(truck).initialQuantity(20).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(alpha).equipmentType(rifle).initialQuantity(500).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(alpha).equipmentType(ammo556).initialQuantity(50000).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(alpha).equipmentType(ammo155).initialQuantity(1000).asOfDate(baselineDate).build());

        // Bravo Base Initial Stock
        initialInventoryRepository.save(InitialInventory.builder().base(bravo).equipmentType(jeep).initialQuantity(20).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(bravo).equipmentType(truck).initialQuantity(10).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(bravo).equipmentType(rifle).initialQuantity(250).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(bravo).equipmentType(ammo556).initialQuantity(25000).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(bravo).equipmentType(ammo155).initialQuantity(400).asOfDate(baselineDate).build());

        // Charlie Base Initial Stock
        initialInventoryRepository.save(InitialInventory.builder().base(charlie).equipmentType(jeep).initialQuantity(15).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(charlie).equipmentType(truck).initialQuantity(5).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(charlie).equipmentType(rifle).initialQuantity(150).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(charlie).equipmentType(ammo556).initialQuantity(15000).asOfDate(baselineDate).build());
        initialInventoryRepository.save(InitialInventory.builder().base(charlie).equipmentType(ammo155).initialQuantity(200).asOfDate(baselineDate).build());

        // 5. Seed Purchases
        Purchase p1 = purchaseRepository.save(Purchase.builder()
                .purchaseReference("PUR-2026-001")
                .base(alpha)
                .equipmentType(jeep)
                .quantity(10)
                .unitCost(BigDecimal.valueOf(45000.00))
                .purchaseDate(LocalDateTime.of(2026, 1, 15, 10, 30))
                .supplierDetails("General Tactical Defense Ltd")
                .recordedByUser(logisticsOfficer)
                .build());

        Purchase p2 = purchaseRepository.save(Purchase.builder()
                .purchaseReference("PUR-2026-002")
                .base(bravo)
                .equipmentType(ammo556)
                .quantity(5000)
                .unitCost(BigDecimal.valueOf(1.20))
                .purchaseDate(LocalDateTime.of(2026, 2, 10, 14, 0))
                .supplierDetails("Federal Munitions Corp")
                .recordedByUser(logisticsOfficer)
                .build());

        // 6. Seed Transfers
        Transfer t1 = transferRepository.save(Transfer.builder()
                .transferReference("TRN-2026-001")
                .sourceBase(alpha)
                .destinationBase(bravo)
                .equipmentType(jeep)
                .quantity(5)
                .transferDate(LocalDateTime.of(2026, 2, 20, 9, 15))
                .remarks("Reinforcement for highland patrol division")
                .initiatedByUser(commanderAlpha)
                .build());

        Transfer t2 = transferRepository.save(Transfer.builder()
                .transferReference("TRN-2026-002")
                .sourceBase(bravo)
                .destinationBase(charlie)
                .equipmentType(ammo556)
                .quantity(2000)
                .transferDate(LocalDateTime.of(2026, 3, 1, 11, 45))
                .remarks("Coastal reserve supply replenishment")
                .initiatedByUser(logisticsOfficer)
                .build());

        // 7. Seed Personnel Assignments
        Assignment a1 = assignmentRepository.save(Assignment.builder()
                .assignmentReference("ASN-2026-001")
                .base(alpha)
                .equipmentType(rifle)
                .personnelName("Sgt. John Miller")
                .personnelRank("Sergeant")
                .personnelId("MIL-8821")
                .assignedQuantity(2)
                .status(AssignmentStatus.ACTIVE)
                .assignedDate(LocalDateTime.of(2026, 3, 5, 8, 0))
                .assignedByUser(commanderAlpha)
                .build());

        Assignment a2 = assignmentRepository.save(Assignment.builder()
                .assignmentReference("ASN-2026-002")
                .base(bravo)
                .equipmentType(jeep)
                .personnelName("Capt. Sarah Jenkins")
                .personnelRank("Captain")
                .personnelId("MIL-4092")
                .assignedQuantity(1)
                .status(AssignmentStatus.ACTIVE)
                .assignedDate(LocalDateTime.of(2026, 3, 10, 13, 30))
                .assignedByUser(commanderBravo)
                .build());

        // 8. Seed Expenditures
        expenditureRepository.save(Expenditure.builder()
                .expenditureReference("EXP-2026-001")
                .base(alpha)
                .equipmentType(ammo556)
                .assignment(null)
                .expendedQuantity(500)
                .reason("Live-fire tactical squad training exercise")
                .expendedDate(LocalDateTime.of(2026, 3, 12, 16, 0))
                .recordedByUser(commanderAlpha)
                .build());

        expenditureRepository.save(Expenditure.builder()
                .expenditureReference("EXP-2026-002")
                .base(bravo)
                .equipmentType(ammo155)
                .assignment(null)
                .expendedQuantity(10)
                .reason("Artillery target practice range calibration")
                .expendedDate(LocalDateTime.of(2026, 3, 18, 10, 15))
                .recordedByUser(commanderBravo)
                .build());

        // 9. Seed Audit Logs
        auditLogRepository.save(AuditLog.builder()
                .userId(logisticsOfficer.getId())
                .username(logisticsOfficer.getUsername())
                .action("RECORD_PURCHASE")
                .entityType("PURCHASE")
                .entityId(p1.getId())
                .baseId(alpha.getId())
                .details("Purchased 10 Tactical Light Vehicles for Fort Alpha Command")
                .ipAddress("127.0.0.1")
                .build());

        auditLogRepository.save(AuditLog.builder()
                .userId(commanderAlpha.getId())
                .username(commanderAlpha.getUsername())
                .action("INITIATE_TRANSFER")
                .entityType("TRANSFER")
                .entityId(t1.getId())
                .baseId(alpha.getId())
                .details("Transferred 5 Tactical Light Vehicles from Fort Alpha Command to Bravo Outpost")
                .ipAddress("127.0.0.1")
                .build());
    }
}
