package com.mapu.domain.follow.application.response;

import lombok.*;

@Getter
@NoArgsConstructor
public class FollowUserDTO {
    private Long userId;
    private String nickName;
    private String imgUrl;
    private String profileId;

    @Builder
    public FollowUserDTO(Long userId, String nickname, String imgUrl, String profileId ) {
        this.userId = userId;
        this.nickName = nickname;
        this.imgUrl = imgUrl;
        this.profileId = profileId;
    }
}