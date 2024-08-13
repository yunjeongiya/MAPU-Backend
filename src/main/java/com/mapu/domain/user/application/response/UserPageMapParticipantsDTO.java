package com.mapu.domain.user.application.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserPageMapParticipantsDTO {
    String imageUrl;
    String nickname;

    @Builder
    public UserPageMapParticipantsDTO(String imageUrl, String nickname) {
        this.imageUrl = imageUrl;
        this.nickname = nickname;
    }
}
