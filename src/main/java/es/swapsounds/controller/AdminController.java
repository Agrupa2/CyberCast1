package es.swapsounds.controller;

import es.swapsounds.DTO.AdminUserViewDTO;
import es.swapsounds.model.User;
import es.swapsounds.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Toda ruta en /admin solo para ROLE_ADMIN
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /** Listado de todos los usuarios **/
    @GetMapping("/users")
    public String listUsers(Principal principal, Model model) {
        if (principal == null) {
            // 401 si no está autenticado
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
        }
        // Si no es ADMIN, 403 Forbidden
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para acceder");
        }

        // Si es admin, renderizamos la lista
        model.addAttribute("username", principal.getName());
        List<User> users = userService.getAllUsers();
        List<AdminUserViewDTO> views = users.stream()
                .map(AdminUserViewDTO::new)
                .toList();
                
        model.addAttribute("users", views);
        return "admin-users";
    }

    /** Eliminar un usuario por ID **/
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
            Principal principal,
            Model model) {
        // El admin no podrá borrarse a sí mismo
        if (userService.findUserByUsername(principal.getName())
                .map(u -> u.getUserId() == id)
                .orElse(false)) {
            model.addAttribute("error", "No puedes eliminar tu propia cuenta");
            return "redirect:/admin/users";
        }

        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
