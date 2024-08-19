package com.mapu.domain.map.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MapBasicInfoResponseDTO {
    private long mapId;
    private String title;
    private String address;
    private String description;
    private double latitude;
    private double longitude;
    private boolean isPublished;
    private boolean isMine;
    private boolean isBookmarked;
    private MapOwnerResponseDTO owner;
}
