package com.scramble.controller;

import com.scramble.dto.User.InterestRequest;
import com.scramble.dto.User.UpdateProfileRequest;
import com.scramble.dto.User.UserResponse;
import com.scramble.entity.DefaultProfileImage;
import com.scramble.entity.User;
import com.scramble.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/interests")
    public ResponseEntity<String> setInterests(@RequestBody InterestRequest interestRequest) {
        userService.setUserInterests(interestRequest);
        return ResponseEntity.ok("Interests updated");
    }

    @GetMapping
    public ResponseEntity<UserResponse> getUserDetails(){
        return ResponseEntity.ok(userService.getUserDetails());
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {

        userService.updateProfile(request);

        return ResponseEntity.ok("Profile updated successfully");
    }

    @GetMapping("/default-images")
    public ResponseEntity<List<DefaultProfileImage>> getAllImages() {
        return ResponseEntity.ok(userService.getAllImages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<?> deleteProfileImage() {
        userService.deleteProfileImage();
        return ResponseEntity.ok("Profile image deleted");
    }

    @DeleteMapping("/cover-image")
    public ResponseEntity<?> deleteCoverImage() {
        userService.deleteCoverImage();
        return ResponseEntity.ok("Cover image deleted");
    }
}
