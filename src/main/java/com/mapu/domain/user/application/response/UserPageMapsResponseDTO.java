package com.mapu.domain.user.application.response;

import com.mapu.domain.map.domain.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class UserPageMapsResponseDTO {
    private Long mapId;
    private String imageUrl;
    private String title;
    private String region;
    private Role role;
    private List<UserPageMapParticipantsDTO> participants;
    private LocalDate createdDate;
    private double longitude;
    private double latitude;

    @Builder
    public UserPageMapsResponseDTO(Long mapId, String imageUrl, String title, String region, Role role, List<UserPageMapParticipantsDTO> participants, LocalDate createdDate, double longitude, double latitude) {
        this.mapId = mapId;
        this.imageUrl = imageUrl;
        this.title = title;
        this.region = region;
        this.role = role;
        this.participants = participants;
        this.createdDate = createdDate;
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
