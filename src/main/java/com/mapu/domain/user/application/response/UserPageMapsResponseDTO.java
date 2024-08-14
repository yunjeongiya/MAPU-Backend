package com.mapu.domain.user.application.response;

import com.mapu.domain.map.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class UserPageMapsResponseDTO {
    private Long mapId;
    private String imageUrl;
    private String title;
    private String region;
    private Role role;
    private List<UserPageMapParticipantsDTO> participants;
    private LocalDateTime createdDate;
    private double longitude;
    private double latitude;

    @Builder
    public UserPageMapsResponseDTO(UserPageMapsDTO userPageMapsDTO, List<UserPageMapParticipantsDTO> participants) {
        this.mapId = userPageMapsDTO.getMapId();
        this.imageUrl = userPageMapsDTO.getImageUrl();
        this.title = userPageMapsDTO.getTitle();
        this.region = userPageMapsDTO.getRegion();
        this.role = userPageMapsDTO.getRole();
        this.createdDate = userPageMapsDTO.getCreatedDate();
        this.latitude = userPageMapsDTO.getLatitude();
        this.longitude = userPageMapsDTO.getLongitude();
        this.participants = participants;
    }
}
