package es.swapsounds.dto;

import java.util.List;

public record UserDTO(
                Long userId,
                String username,
                String email,
                List<String> roles) {
}