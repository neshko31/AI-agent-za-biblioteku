package com.library.requestservice.repository;
import com.library.requestservice.model.BorrowRequest;
import com.library.requestservice.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, String> {
    List<BorrowRequest> findAllByStatus(RequestStatus status);
    List<BorrowRequest> findAllByReceiverLibraryIdOrderByCreatedAtDesc(String receiverLibraryId);
    List<BorrowRequest> findAllBySenderLibraryIdOrderByCreatedAtDesc(String senderLibraryId);
}
