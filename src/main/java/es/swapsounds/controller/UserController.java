package es.swapsounds.controller;

import es.swapsounds.storage.InMemoryCommentRepository;
import es.swapsounds.storage.InMemoryStorage;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

   @Autowired
   private InMemoryCommentRepository inMemoryCommentRepository;

   @Autowired
   private InMemoryStorage storage;

   @GetMapping("/delete-account")
   public String showDeletePage(HttpSession session, Model model) {
      Long userId = (Long) session.getAttribute("userId");
      if (userId == null)
         return "redirect:/login";

      model.addAttribute("username", session.getAttribute("username"));
      return "delete-account";
   }

   // Procesar eliminación
   @PostMapping("/delete-account")
   public String processDelete(
         @RequestParam String confirmation,
         HttpSession session,
         RedirectAttributes ra) {

      Long userId = (Long) session.getAttribute("userId");
      if (userId == null)
         return "redirect:/login";

      if (!"ELIMINAR CUENTA".equals(confirmation.trim())) {
         ra.addFlashAttribute("error", "Debes escribir exactamente 'ELIMINAR CUENTA'");
         return "redirect:/delete-account";
      }

      inMemoryCommentRepository.deleteCommentsByUserId(userId);
      storage.deleteUser(userId);
      session.invalidate();

      ra.addFlashAttribute("success", "¡Cuenta eliminada permanentemente!");
      return "redirect:/";
   }

}