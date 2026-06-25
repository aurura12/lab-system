package com.lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertUpdateScheduler {

    private final ReagentInventoryService inventoryService;

    /**
     * 每天凌晨 2:00 执行一次，更新所有库存的效期预警级别。
     * 试剂效期以天为单位变化，每日更新一次完全够用。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void updateAlertLevels() {
        log.info("Starting daily alert level update...");
        inventoryService.updateAlertLevels();
        log.info("Daily alert level update completed.");
    }
}
