package es.swapsounds.dto;

public record UserRegistrationDTO(
        String username,
        String email,
        String password) {
}
