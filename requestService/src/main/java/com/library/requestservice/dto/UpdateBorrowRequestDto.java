package com.library.requestservice.dto;
import com.library.requestservice.model.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class UpdateBorrowRequestDto {
    private String respondedByLibrarianId;
    @NotNull(message = "Status must not be null")
    private RequestStatus status;
    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;
}
