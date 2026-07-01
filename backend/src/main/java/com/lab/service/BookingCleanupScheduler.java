package com.lab.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingCleanupScheduler {

    private final EquipmentBookingService bookingService;

    /**
     * 每 10 分钟检查一次，自动释放超过 30 分钟未签到的已批准预约。
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void releaseNoShows() {
        int count = bookingService.releaseNoShows();
        if (count > 0) {
            log.info("Released {} no-show bookings", count);
        }
    }
}
