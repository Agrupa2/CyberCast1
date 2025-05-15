package es.swapsounds.DTO;

import java.util.List;

public record UserDTO(
        Long userId,
        String username,
        String email,
        List<String> roles) {
}