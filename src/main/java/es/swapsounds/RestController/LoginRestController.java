package es.swapsounds.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.swapsounds.security.jwt.AuthResponse;
import es.swapsounds.security.jwt.AuthResponse.Status;
import es.swapsounds.service.AuthService;
import es.swapsounds.security.jwt.LoginRequest;
import es.swapsounds.security.jwt.UserLoginService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class LoginRestController {

	@Autowired
	private UserLoginService userService;

	@Autowired
	private AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {

		return userService.login(response, loginRequest);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refreshToken(
			@CookieValue(name = "RefreshToken", required = false) String refreshToken, HttpServletResponse response) {

		return userService.refresh(response, refreshToken);
	}

	@PostMapping("/logout")
	public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
		return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userService.logout(response)));
	}

	@PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) MultipartFile profilePhoto,
            HttpServletResponse response) {

        return authService.signup(username, email, password, profilePhoto, response);
    }

}