package com.library.requestservice.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.Instant;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "borrow_requests")
public class BorrowRequest implements Serializable {
    @Id
    private String id;
    @Column(nullable = false)
    private String senderId;
    @Column(nullable = false)
    private String senderLibraryId;
    @Column(nullable = false)
    private String receiverLibraryId;
    private String respondedByLibrarianId;
    @Column(nullable = false, length = 300)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
}
