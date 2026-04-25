package com.scramble.dto.User;

import lombok.Data;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String name;
    private String bio;
    private String profileImage;
    private String coverImage;
    private List<String> interests;
    private String instagram;
    private String twitter;
    private String linkedin;
}