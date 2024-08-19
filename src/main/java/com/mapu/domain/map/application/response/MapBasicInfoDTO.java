package com.mapu.domain.map.application.response;

import com.mapu.domain.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MapBasicInfoDTO {
    private long mapId;
    private String title;
    private String address;
    private String description;
    private double latitude;
    private double longitude;
    private User owner;

}
