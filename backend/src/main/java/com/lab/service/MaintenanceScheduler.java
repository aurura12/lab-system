package com.lab.service;

import com.lab.entity.Equipment;
import com.lab.entity.MaintenanceOrder;
import com.lab.repository.EquipmentRepository;
import com.lab.repository.MaintenanceOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceScheduler {

    private final EquipmentRepository equipmentRepository;
    private final MaintenanceOrderRepository orderRepository;

    /**
     * 每天早上 8:00 检查未来 7 天内需要维保的设备，
     * 如果没有对应的待处理/进行中工单，自动创建。
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void autoCreateMaintenanceOrders() {
        LocalDate deadline = LocalDate.now().plusDays(7);
        List<Equipment> dueEquipment = equipmentRepository.findByNextMaintenanceBefore(deadline);

        int created = 0;
        for (Equipment eq : dueEquipment) {
            boolean hasActive = orderRepository.existsByEquipmentIdAndStatusIn(
                    eq.getId(), List.of("pending", "in_progress"));
            if (hasActive) continue;

            MaintenanceOrder order = new MaintenanceOrder();
            order.setEquipment(eq);
            order.setTitle("定期维保 - " + eq.getName());
            order.setType("routine");
            order.setPriority("medium");
            order.setStatus("pending");
            order.setScheduledDate(eq.getNextMaintenance());
            order.setDescription("系统自动创建的定期维保工单");
            orderRepository.save(order);
            created++;
        }

        if (created > 0) {
            log.info("Auto-created {} maintenance orders for upcoming equipment", created);
        }
    }
}
