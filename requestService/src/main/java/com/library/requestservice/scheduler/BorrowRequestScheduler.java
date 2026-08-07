package com.library.requestservice.scheduler;

import com.library.requestservice.model.BorrowRequest;
import com.library.requestservice.model.RequestStatus;
import com.library.requestservice.repository.BorrowRequestCacheRepository;
import com.library.requestservice.repository.BorrowRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class BorrowRequestScheduler {

    private final BorrowRequestRepository repository;
    private final BorrowRequestCacheRepository cache;
    private final Duration expiryDuration;

    public BorrowRequestScheduler(BorrowRequestRepository repository,
                                   BorrowRequestCacheRepository cache,
                                   @Value("${app.request.expiry-hours:72}") long expiryHours) {
        this.repository = repository;
        this.cache = cache;
        this.expiryDuration = Duration.ofHours(expiryHours);
    }

    @Scheduled(cron = "${app.request.expiry-cron:0 0 * * * *}")
    @Transactional
    public void expireStaleRequests() {
        Instant cutoff = Instant.now().minus(expiryDuration);
        List<BorrowRequest> pending = repository.findAllByStatus(RequestStatus.PENDING);
        for (BorrowRequest request : pending) {
            if (request.getCreatedAt().isBefore(cutoff)) {
                request.setStatus(RequestStatus.EXPIRED);
                request.setUpdatedAt(Instant.now());
                repository.save(request);
                cache.put(request);
            }
        }
    }
}
