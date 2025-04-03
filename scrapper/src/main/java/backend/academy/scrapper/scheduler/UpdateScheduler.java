package backend.academy.scrapper.scheduler;

import backend.academy.scrapper.service.UpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class UpdateScheduler {
    private final UpdateService updateService;

    @Scheduled(fixedDelayString = "${scheduler.update-interval}")
    public void checkForUpdates() {
        log.info("Starting scheduled update check");
        updateService.checkUpdates();
    }
}
