package com.mapu.domain.map.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapOwnerResponseDTO {
    private long userId;
    private String imageUrl;
    private String nickName;
    private String profileId;
}
