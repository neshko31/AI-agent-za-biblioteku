package com.library.requestservice.controller;
import com.library.requestservice.dto.CreateBorrowRequestDto;
import com.library.requestservice.dto.UpdateBorrowRequestDto;
import com.library.requestservice.model.BorrowRequest;
import com.library.requestservice.service.BorrowRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/requests")
public class BorrowRequestController {
    private final BorrowRequestService service;
    public BorrowRequestController(BorrowRequestService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<BorrowRequest> createRequest(@Valid @RequestBody CreateBorrowRequestDto dto) {
        BorrowRequest created = service.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    @GetMapping("/{id}")
    public ResponseEntity<BorrowRequest> getRequestById(@PathVariable String id) {
        BorrowRequest request = service.getRequestById(id);
        return ResponseEntity.ok(request);
    }
    @GetMapping
    public ResponseEntity<List<BorrowRequest>> getAllRequests() {
        List<BorrowRequest> requests = service.getAllRequests();
        return ResponseEntity.ok(requests);
    }
    @GetMapping("/incoming/{libraryId}")
    public ResponseEntity<List<BorrowRequest>> getIncoming(@PathVariable String libraryId) {
        return ResponseEntity.ok(service.getIncomingForLibrary(libraryId));
    }
    @GetMapping("/outgoing/{libraryId}")
    public ResponseEntity<List<BorrowRequest>> getOutgoing(@PathVariable String libraryId) {
        return ResponseEntity.ok(service.getOutgoingForLibrary(libraryId));
    }
    @PostMapping("/{id}/cancel")
    public ResponseEntity<BorrowRequest> cancelRequest(@PathVariable String id) {
        return ResponseEntity.ok(service.cancelRequest(id));
    }
    @PutMapping("/{id}")
    public ResponseEntity<BorrowRequest> updateRequest(
            @PathVariable String id,
            @Valid @RequestBody UpdateBorrowRequestDto dto) {
        BorrowRequest updated = service.updateRequest(id, dto);
        return ResponseEntity.ok(updated);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable String id) {
        service.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}
