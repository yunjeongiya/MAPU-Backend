package com.mapu.domain.home.application.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
public class HomeKeywordMapResponseDTO {
    private String keyword;
    private List<KeywordMapDTO> maps;

    @Builder
    @Getter
    public static class KeywordMapDTO {
        private String nickname;
        private String profileId;
        private String userImage;
        private Long mapId;
        private String mapTitle;
        private String mapImage;
//        지도 설명, 지도 주소, 지도에 대한 지도 대표 키워드 추가
        private String mapDescription;
        private String mapAddress;
        private List<String> mapKeywords;
    }
}