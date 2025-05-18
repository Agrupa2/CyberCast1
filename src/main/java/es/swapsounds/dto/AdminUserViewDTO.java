package es.swapsounds.dto;

import es.swapsounds.model.User;

public class AdminUserViewDTO {
    private final Long userId;
    private final String username;
    private final String email;
    private final String avatarUrl; // null si no tiene
    private final String userInitial;

    public AdminUserViewDTO(User u) {
        this.userId = u.getUserId();
        this.username = u.getUsername();
        this.email = u.getEmail();

        // Si tiene foto devolvemos la ruta al endpoint que expone el Blob
        this.avatarUrl = (u.getProfilePicture() != null)
                ? "/users/" + userId + "/avatar"
                : null;

        // Calculamos la inicial
        this.userInitial = (username != null && !username.isBlank())
                ? username.substring(0, 1).toUpperCase()
                : "?";
    }

    // Getters para Mustache:
    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getUserInitial() {
        return userInitial;
    }
}
