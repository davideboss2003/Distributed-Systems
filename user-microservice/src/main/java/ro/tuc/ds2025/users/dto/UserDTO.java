package ro.tuc.ds2025.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String role;
    private String fullName;
    private String email;
    //private LocalDateTime createdAt;
    //private LocalDateTime updatedAt;

}
