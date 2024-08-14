package com.mapu.domain.user.application.response;

import com.mapu.domain.map.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserPageMapsDTO {
    private Long mapId;
    private String imageUrl;
    private String title;
    private String region;
    private Role role;
    private LocalDateTime createdDate;
    private double longitude;
    private double latitude;
}
