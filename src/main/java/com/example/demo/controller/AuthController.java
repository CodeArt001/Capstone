package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.web.server.MimeMappings.Mapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.entity.User;
import com.example.demo.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()

        );
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getid());
        response.put("email", user.getEmail());
        response.put("message", "Registration Successful");
        return ResponseEntity.ok(response);

    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);

        return ResponseEntity.ok(response);
    }

   
   @GetMapping("/verify-email")
   public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
       String result = authService.verifyEmail(token);
       return ResponseEntity.ok(result);
   }

   @PostMapping("/forgot-password")
   public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
       String result = authService.forgotPassword(email);
       return ResponseEntity.ok(result);
   }

   @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(result);
    }
    
 @GetMapping("/reset-password")
public ResponseEntity<String> showResetPasswordForm(@RequestParam("token") String token) {
    String html = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
            "<title>Reset Password</title>" +
            "<style>" +
            "body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f4f4f9; margin: 0; }" +
            ".card { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); width: 300px; }" +
            "input { width: 100%; padding: 10px; margin: 10px 0; box-sizing: border-box; }" +
            "button { width: 100%; padding: 10px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<div class=\"card\">" +
            "<h2>Reset Password</h2>" +
            "<form action=\"/auth/reset-password-submit\" method=\"POST\">" +
            "<input type=\"hidden\" name=\"token\" value=\"" + token + "\" />" +
            "<label>New Password</label>" +
            "<input type=\"password\" name=\"newPassword\" placeholder=\"Enter new password\" required />" +
            "<button type=\"submit\">Submit</button>" +
            "</form>" +
            "</div>" +
            "</body>" +
            "</html>";

    return ResponseEntity.ok().header("Content-Type", "text/html").body(html);
}

@PostMapping(value = "/reset-password-submit", consumes = "application/x-www-form-urlencoded")
public ResponseEntity<String> handleResetPasswordForm(
        @RequestParam("token") String token, 
        @RequestParam("newPassword") String newPassword) {
    
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken(token);
    request.setNewPassword(newPassword);
    
    authService.resetPassword(request);
    
    return ResponseEntity.ok("""
        <!DOCTYPE html>
        <html>
        <body style="font-family: Arial, sans-serif; text-align: center; margin-top: 50px;">
            <h2 style="color: green;">Password Reset Successfully!</h2>
            <p style="font-weight:bold">cheers 🤣💗🏋️⭐✔️.</p>
        </body>
        </html>
        """);
}

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam("email") String email) {
        String result = authService.logout(email);
        return ResponseEntity.ok(result);
    }
}
