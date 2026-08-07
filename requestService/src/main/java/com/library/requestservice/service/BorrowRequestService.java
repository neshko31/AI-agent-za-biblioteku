package com.library.requestservice.service;
import com.library.requestservice.dto.CreateBorrowRequestDto;
import com.library.requestservice.dto.UpdateBorrowRequestDto;
import com.library.requestservice.exception.InvalidStatusTransitionException;
import com.library.requestservice.exception.RequestNotFoundException;
import com.library.requestservice.model.BorrowRequest;
import com.library.requestservice.model.RequestStatus;
import com.library.requestservice.repository.BorrowRequestCacheRepository;
import com.library.requestservice.repository.BorrowRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Service
public class BorrowRequestService {
    private final BorrowRequestRepository repository;
    private final BorrowRequestCacheRepository cache;
    public BorrowRequestService(BorrowRequestRepository repository,
                                BorrowRequestCacheRepository cache) {
        this.repository = repository;
        this.cache = cache;
    }
    @Transactional
    public BorrowRequest createRequest(CreateBorrowRequestDto dto) {
        BorrowRequest request = BorrowRequest.builder()
                .id(UUID.randomUUID().toString())
                .senderId(dto.getSenderId())
                .senderLibraryId(dto.getSenderLibraryId())
                .receiverLibraryId(dto.getReceiverLibraryId())
                .description(dto.getDescription())
                .status(RequestStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        BorrowRequest saved = repository.save(request);
        cache.put(saved);
        return saved;
    }
    public BorrowRequest getRequestById(String id) {
        return cache.get(id).orElseGet(() -> {
            BorrowRequest fromDb = repository.findById(id)
                    .orElseThrow(() -> new RequestNotFoundException(id));
            cache.put(fromDb);
            return fromDb;
        });
    }
    public List<BorrowRequest> getAllRequests() {
        return repository.findAll();
    }

    public List<BorrowRequest> getIncomingForLibrary(String libraryId) {
        return repository.findAllByReceiverLibraryIdOrderByCreatedAtDesc(libraryId);
    }

    public List<BorrowRequest> getOutgoingForLibrary(String libraryId) {
        return repository.findAllBySenderLibraryIdOrderByCreatedAtDesc(libraryId);
    }

    @Transactional
    public BorrowRequest cancelRequest(String id) {
        BorrowRequest existing = repository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
        if (existing.getStatus() == RequestStatus.EXPIRED) {
            throw new InvalidStatusTransitionException(
                    "Cannot cancel a request that has already expired.");
        }
        if (existing.getStatus() == RequestStatus.CANCELLED) {
            throw new InvalidStatusTransitionException(
                    "Request is already cancelled.");
        }
        existing.setStatus(RequestStatus.CANCELLED);
        existing.setUpdatedAt(Instant.now());
        BorrowRequest updated = repository.save(existing);
        cache.put(updated);
        return updated;
    }
    @Transactional
    public BorrowRequest updateRequest(String id, UpdateBorrowRequestDto dto) {
        BorrowRequest existing = repository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));
        validateStatusTransition(existing.getStatus(), dto.getStatus());
        if ((dto.getStatus() == RequestStatus.ACCEPTED || dto.getStatus() == RequestStatus.DENIED)
                && (dto.getRespondedByLibrarianId() == null || dto.getRespondedByLibrarianId().isBlank())) {
            throw new InvalidStatusTransitionException(
                    "A responding librarian ID is required when accepting or denying a request."
            );
        }
        existing.setStatus(dto.getStatus());
        existing.setUpdatedAt(Instant.now());
        if (dto.getRespondedByLibrarianId() != null && !dto.getRespondedByLibrarianId().isBlank()) {
            existing.setRespondedByLibrarianId(dto.getRespondedByLibrarianId());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            existing.setDescription(dto.getDescription());
        }
        BorrowRequest updated = repository.save(existing);
        cache.put(updated);
        return updated;
    }
    @Transactional
    public void deleteRequest(String id) {
        if (!repository.existsById(id)) {
            throw new RequestNotFoundException(id);
        }
        repository.deleteById(id);
        cache.evict(id);
    }
    private void validateStatusTransition(RequestStatus current, RequestStatus next) {
        if (current != RequestStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot change status from %s. Only PENDING requests can be updated.", current)
            );
        }
        if (next == RequestStatus.PENDING) {
            throw new InvalidStatusTransitionException("Cannot set status back to PENDING.");
        }
    }
}
