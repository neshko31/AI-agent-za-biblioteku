package com.library.requestservice.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class CreateBorrowRequestDto {
    @NotBlank(message = "Sender ID must not be blank")
    private String senderId;
    @NotBlank(message = "Sender library ID must not be blank")
    private String senderLibraryId;
    @NotBlank(message = "Receiver library ID must not be blank")
    private String receiverLibraryId;
    @NotBlank(message = "Description must not be blank")
    @Size(min = 5, max = 300, message = "Description must be between 5 and 300 characters")
    private String description;
}
