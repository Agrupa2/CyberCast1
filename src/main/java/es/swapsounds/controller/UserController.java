package es.swapsounds.controller;

import es.swapsounds.service.CommentService;
import es.swapsounds.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

   @Autowired
   private CommentService commentService;

   @Autowired
   private UserService userService;

   @GetMapping("/delete-account")
   public String showDeletePage(Principal principal, HttpSession session, Model model) {
      Long userId = userService.getUserIdFromPrincipal(principal);
      if (userId == null)
         return "redirect:/login";

      model.addAttribute("username", principal.getName());
      return "delete-account";
   }

   // Procesar eliminación
   @PostMapping("/delete-account")
   public String processDelete(
         @RequestParam String confirmation,
         Principal principal,
         HttpServletRequest request,
         HttpServletResponse response,
         RedirectAttributes ra) {

      if (principal == null)
         return "redirect:/login";

      if (!"ELIMINAR CUENTA".equals(confirmation.trim())) {
         ra.addFlashAttribute("error", "Debes escribir exactamente 'ELIMINAR CUENTA'");
         return "redirect:/delete-account";
      }

      Long userId = userService.findUserByUsername(principal.getName()).get().getUserId();

      commentService.deleteCommentsByUserId(userId);
      userService.deleteUser(userId);
      new SecurityContextLogoutHandler().logout(request, response, null);

      ra.addFlashAttribute("success", "¡Cuenta eliminada permanentemente!");
      return "redirect:/";
   }

}