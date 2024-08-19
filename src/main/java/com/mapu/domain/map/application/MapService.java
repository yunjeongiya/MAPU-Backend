package com.mapu.domain.map.application;

import com.mapu.domain.follow.dao.FollowRepository;
import com.mapu.domain.map.api.request.CreateMapRequestDTO;
import com.mapu.domain.map.application.response.MapBasicInfoDTO;
import com.mapu.domain.map.application.response.MapBasicInfoResponseDTO;
import com.mapu.domain.map.application.response.MapListResponseDTO;
import com.mapu.domain.map.application.response.MapOwnerResponseDTO;
import com.mapu.domain.map.dao.MapKeywordRepository;
import com.mapu.domain.map.dao.MapUserBookmarkRepository;
import com.mapu.domain.map.dao.MapRepository;
import com.mapu.domain.map.dao.MapUserRoleRepository;
import com.mapu.domain.map.domain.Map;
import com.mapu.domain.map.domain.MapUserBookmark;
import com.mapu.domain.map.domain.MapUserRole;
import com.mapu.domain.map.domain.Role;
import com.mapu.domain.map.exception.MapException;
import com.mapu.domain.map.exception.errcode.MapExceptionErrorCode;
import com.mapu.domain.user.dao.UserRepository;
import com.mapu.domain.user.domain.User;
import com.mapu.global.jwt.dto.JwtUserDto;
import com.mapu.infra.oauth.config.OAuthClientConfig;
import com.mapu.infra.s3.application.S3ByteArrayMultipartFile;
import com.mapu.infra.s3.application.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MapService {

    private final MapRepository mapRepository;
    private final MapKeywordRepository keywordRepository;
    private final UserRepository userRepository;
    private final MapUserBookmarkRepository mapUserBookmarkRepository;
    private final MapUserRoleService mapUserRoleService;
    private final S3Service s3Service;
    private final OAuthClientConfig oAuthClientConfig;
    private final FollowRepository followRepository;
    private final MapUserRoleRepository mapUserRoleRepository;

    public List<MapListResponseDTO> getMapList(String searchType, Pageable pageable, String searchWord) {
        switch (searchType) {
            case "RANDOM": {
                List<MapListResponseDTO> mapList = getMapListByRandom(pageable, searchWord, -1);
                return mapList;
            }
            case "DATE": {
                List<MapListResponseDTO> mapList = getMapListByDate(pageable, searchWord, -1);
                return mapList;
            }
            default:
                throw new MapException(MapExceptionErrorCode.SOCIALTYPE_ERROR);
        }
    }

    public List<MapListResponseDTO> getMapListForLoginedUser(JwtUserDto jwtUserDto, String searchType, Pageable pageable, String searchWord) {
        long userId = Integer.parseInt(jwtUserDto.getName());

        switch (searchType) {
            case "RANDOM": {
                List<MapListResponseDTO> mapList = getMapListByRandom(pageable, searchWord, userId);
                return mapList;
            }
            case "DATE": {
                List<MapListResponseDTO> mapList = getMapListByDate(pageable, searchWord, userId);
                return mapList;
            }
            default:
                throw new MapException(MapExceptionErrorCode.SOCIALTYPE_ERROR);
        }
    }

    private List<MapListResponseDTO> getMapListByDate(Pageable pageable, String searchWord, long userId) {
        // AnaymousUser case
        if (userId < 0){
            List<Map> maps = mapRepository.findAllByOrderByCreatedAtDesc(searchWord,pageable);
            log.info("MapService findAllByOrderByCreatedAtDesc - Retrieved {} map(s) from the database", maps.size());
            return maps.stream().map(this::mapConvertToDTO).collect(Collectors.toList());
        }
        // LoginedUser case
        if (userId > 0){
            List<Map> maps = mapRepository.findAllByOrderByCreatedAtDescForLoginedUser(searchWord, userId, pageable);
            log.info("MapService findAllByOrderByCreatedAtDescForLoginedUser - Retrieved {} map(s) from the database", maps.size());
            return maps.stream().map(this::mapConvertToDTO).collect(Collectors.toList());
        }
        throw new MapException(MapExceptionErrorCode.NO_EXIST_MAP);
    }

    private List<MapListResponseDTO> getMapListByRandom(Pageable pageable, String searchWord, long userId) {
        // AnaymousUser case
        if (userId < 0){
            List<Map> maps = mapRepository.findAllByRandom(searchWord, pageable);
            log.info("MapService GetMapListByRandom - Retrieved {} map(s) from the database", maps.size());
            return maps.stream().map(this::mapConvertToDTO).collect(Collectors.toList());
        }
        // LoginedUser case
        if (userId > 0){
            List<Map> maps = mapRepository.findAllByRandomForLoginedUser(searchWord, userId, pageable);
            log.info("MapService findAllByRandomForLoginedUser - Retrieved {} map(s) from the database", maps.size());
            return maps.stream().map(this::mapConvertToDTO).collect(Collectors.toList());
        }
        throw new MapException(MapExceptionErrorCode.NO_EXIST_MAP);
    }

    private MapListResponseDTO mapConvertToDTO(Map map) {

        MapOwnerResponseDTO mapOwnerDTO = null;
        User user = map.getUser();
        if(user != null) {
            mapOwnerDTO = MapOwnerResponseDTO.builder()
                    .userId(user.getId())
                    .imageUrl(user.getImage())
                    .nickName(user.getNickname())
                    .profileId(user.getProfileId())
                    .build();
        }

        List<String> keywords = keywordRepository.findKeywordsByMapId(map.getId());
        log.info("MapService mapConvertToDTO - Retrieved keywords from the database");
        log.info("keywords = " + keywords);

        return MapListResponseDTO.builder()
                .mapId(map.getId())
                .title(map.getMapTitle())
                .region(map.getAddress())
                .description(map.getMapDescription())
                .imageUrl(map.getImageUrl())
                .user(mapOwnerDTO)
                .keyword(keywords)
                .build();
    }

    public void addMapBookmark(long userId, Long mapId) {
        User user = userRepository.findById(userId);
        Map map = mapRepository.findById(mapId).orElseThrow(()-> new MapException(MapExceptionErrorCode.NO_EXIST_MAP));
        if(mapUserBookmarkRepository.existsByMapIdAndUserId(mapId,userId)) throw new MapException(MapExceptionErrorCode.ALREADY_BOOKMARKED);
        MapUserBookmark mapUserBookmark = MapUserBookmark.builder().user(user).map(map).build();
        mapUserBookmarkRepository.save(mapUserBookmark);
    }

    public void removeMapBookmark(long userId, Long mapId) {
        MapUserBookmark mapUserBookmark = mapUserBookmarkRepository.findByUserIdAndMapId(userId, mapId);
        if (mapUserBookmark == null){
            throw new MapException(MapExceptionErrorCode.NOT_FOUND_BOOKMARK);
        }
        mapUserBookmarkRepository.delete(mapUserBookmark);
    }


    public void createMap(CreateMapRequestDTO requestDTO, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저 없음"));

        String imageUrl = getMapImageUrl(requestDTO.getLatitude(), requestDTO.getLongitude(), requestDTO.getZoomLevel());

        Map map = Map.builder()
                .mapTitle(requestDTO.getMapTitle())
                .mapDescription(requestDTO.getMapDescription())
                .address(requestDTO.getAddress())
                .latitude(requestDTO.getLatitude())
                .longitude(requestDTO.getLongitude())
                .zoomLevel(requestDTO.getZoomLevel())
                .publishLink(requestDTO.getPublishLink())
                .isOnSearch(requestDTO.getIsOnSearch())
                .imageUrl(imageUrl)
                .user(user)
                .build();

        MapUserRole mapUserRole = MapUserRole.builder()
                .role(Role.OWNER)
                .user(user)
                .map(map)
                .build();
        mapRepository.save(map);
        mapUserRoleRepository.save(mapUserRole);
        mapUserRoleService.addOwner(map.getId(), user.getNickname());
    }

    private String getMapImageUrl(double latitude, double longitude, int zoomLevel) {

        // 잘못된 image 요청
//        String url = String.format("/v2/maps/static/image?center=%f,%f&level=%d&w=%d&h=%d",
//                longitude, latitude, zoomLevel, 500, 300);
//        WebClient webClient = WebClient.builder().baseUrl("https://dapi.kakao.com").build();


//        byte[] imageData = webClient.get()
//                .uri(url)
//                .header("Authorization", "KakaoAK " + oAuthClientConfig.getKakaoClientId())
//                .retrieve()
//                .bodyToMono(byte[].class)
//                .block();

        // 타임스탬프 생성
//        String timestamp = DateTimeFormatter
//                .ofPattern("yyyyMMddHHmmssSSS")
//                .withZone(ZoneId.systemDefault())
//                .format(Instant.now());

        // 이미지 파일명 생성 map_20240819123456789_37.5665_126.9780_12.jpg
//        String fileName = String.format("map_%s_%.4f_%.4f_%d.jpg", timestamp, latitude, longitude, zoomLevel);

//        MultipartFile multipartFile = new S3ByteArrayMultipartFile(imageData, fileName, fileName, "image/jpeg");

        String example_url = "https://map-static-image-s3-mapu-backend.s3.ap-northeast-2.amazonaws.com/2024-08-20-8.11.45.png?" +
                "response-content-disposition=inline&X-Amz-Security-Token=IQoJb3JpZ2luX2VjEEcaDmFwLW5vcnRoZWFzdC0yIkYwRAIgRcLiXlFvy" +
                "CX198MiP9D6SxfEoN54Dd4LES%2FO1g5gKKYCIE5WkcL4uWVLUCtZhBKw%2BTWOT4PGhb4pjMsnU26bawOQKuQCCFAQABoMNTkwMTgzODA1MTc2IgxEgd" +
                "eqpZ88PkzMXksqwQLSw6T9Hvr6IbhJHcNJCGPTu4iMsxrNMo9HWWIq4MS0k%2BWGQJ45%2BLFfiO2IF5jN8tXljPyyg7RL7XRFcFLAccbw%2FQ0YLxpWOo%2" +
                "F04ZGouQBeUDBlRfGfn0bth1KmGT5Zl4Ga%2BlLNtCDqMEo5z0iWhjgpXnFWHLPSAs7muhg4MAVTLjQ%2B8ydiKQo69Qjql03qFrHnnDdA%2BSMS%2BwMHe7fo" +
                "VhEVr4PAX7%2BLa3VD1HmBfhq8CFsw%2Fhj715K%2FYl9a02PFoD4tDYWsYasykSea3ZxSu6b2aYcIbC9pRoxR%2BOHX%2BYsrb5qtznVHUWHEfb%2FrWJtmkb8jv" +
                "ONMI5wDhNGv0QDcoBQWGkUszPkL6VhlcQ1zX1EGG5OM0uxUS%2FurZ2TyXXe166aTWcEGluuSuYUxMIU7JRcOgBRgZqXv6aIvC2JhUjW0Bh0w5PKMtgY6tAJIkmOGt3o" +
                "PzhuljAJmQjnVJA2w3SsyuKTcZTTo4nffuoq%2BtcVtyTLxOo%2Bq0n6JfyusKpAzqY7wc%2FF9WDhnTQkldJRpsG43HSh8fEMEwf7l4GExLP0cloVavwWB%2FvPx0GFjUU" +
                "dOlMqqFtGz%2BezMYSSvwge0OydOicWpUDbxNF6yl7%2BRHypP3NfCV0o9m0W0uGK%2FlzP1i6whEEjwXbn6AK76V0UpLG8Imm9cquGo%2Bj3U4oU29giS%2F%2F89r4NhnMGx" +
                "gw4lbjyuGfgJcp%2BgsrFkOm1NfUb5ilfA6IbO6v3t1XP49UA0iif012W2oZC6rmzRsPFpqzENpttxBGjVz8rb6tKXaEfbYoA9LVlflDQ5ptXPKakTvy9dlLbIGX7UGwtlDP4z" +
                "fl6lC%2BUdjk2XAf1staIyDpqzow%3D%3D&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20240819T231642Z&X-Amz-SignedHeaders=host&X-Amz-Expires" +
                "=300&X-Amz-Credential=ASIAYS2NSGD4GAOOUS7F%2F20240819%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Signature=a42fa17978022965f85d86e4346d" +
                "42e880977f09f663ba7a3a6eb01f49cde71d";

        return example_url;

//        try {
//            String imageUrl = s3Service.uploadImage(multipartFile);
//            return imageUrl;
//        } catch (Exception e) {
//            log.error("Failed to upload map image to S3", e);
//            throw new MapException(MapExceptionErrorCode.FAILED_UPLOAD_IMAGE);
//        }

        // ByteArrayOutputStream을 사용하여 메모리에 데이터 저장
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(imageData.length)) {
//            byteArrayOutputStream.write(imageData);
//            byteArrayOutputStream.flush();
//
//            // ByteArrayInputStream을 사용하여 MultipartFile 생성
//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
//            MultipartFile multipartFile = new MockMultipartFile(fileName, fileName, "image/jpeg", byteArrayInputStream);
//
//            // S3Service를 사용하여 이미지 업로드
//            String imageUrl = s3Service.uploadImage(multipartFile);
//            return imageUrl;
//        } catch (Exception e) {
//            log.error("Failed to process map image", e);
//            throw new RuntimeException("Failed to process map image", e);
//        }

    }

    public List<MapListResponseDTO> getOtherUserMapList(long otherUserId, Pageable pageable) {
        List<Map> maps = mapRepository.findOtherUserMapsByUserId(otherUserId, pageable);
        log.info("MapService getOtherUserMapList - Retrieved {} map(s) from the database", maps.size());
        return maps.stream().map(this::mapConvertToDTO).collect(Collectors.toList());

    }

    public MapBasicInfoResponseDTO getMapBasicInfo(JwtUserDto jwtUserDto, Long mapId) {
        log.info("MapService getMapBasicInfo - Retrieved map with id {}", mapId);
        log.info("userId {}", mapId);
        long userId = Integer.parseInt(jwtUserDto.getName());
        MapBasicInfoDTO mapBasicInfo = mapRepository.findMapBasicInfoById(mapId);

        if (mapBasicInfo.getOwner().getId() != userId) {
            // 다른 사용자의 map이므로 mapowner 정보 다 넘겨주기
            boolean amIFollowingOwner = followRepository.isFollowingExistByIds(userId, mapBasicInfo.getOwner().getId());
            User user = mapBasicInfo.getOwner();
            MapOwnerResponseDTO owner = MapOwnerResponseDTO.builder()
                    .userId(user.getId())
                    .imageUrl(user.getImage())
                    .nickName(user.getNickname())
                    .profileId(user.getProfileId())
                    .amIFollowing(amIFollowingOwner)
                    .build();

            boolean existByUseIdAndMapId = mapUserBookmarkRepository.isExistByUserIdAndMapId(userId, mapId);
            return MapBasicInfoResponseDTO.builder()
                    .mapId(mapBasicInfo.getMapId())
                    .title(mapBasicInfo.getTitle())
                    .address(mapBasicInfo.getAddress())
                    .description(mapBasicInfo.getDescription())
                    .latitude(mapBasicInfo.getLatitude())
                    .longitude(mapBasicInfo.getLongitude())
                    .isPublished(mapBasicInfo.isOnSearch())
                    .isMine(false)
                    .isBookmarked(existByUseIdAndMapId)
                    .owner(owner)
                    .build();
        }

        //내가 지도 제작자인 경우
        return MapBasicInfoResponseDTO.builder()
                .mapId(mapBasicInfo.getMapId())
                .title(mapBasicInfo.getTitle())
                .address(mapBasicInfo.getAddress())
                .description(mapBasicInfo.getDescription())
                .latitude(mapBasicInfo.getLatitude())
                .longitude(mapBasicInfo.getLongitude())
                .isMine(true)
                .owner(new MapOwnerResponseDTO())
                .build();
    }

    //view에 대한 지도 기본 정보 조회
    public MapBasicInfoResponseDTO getMapBasicInfoForViewer(Long mapId) {
        log.info("MapService getMapBasicInfoForEdit - Retrieved map with id {}", mapId);
        MapBasicInfoDTO mapBasicInfo = mapRepository.findMapBasicInfoById(mapId);

        //뷰어 모드는 내가 무조건 제 3자이므로 mapowner 정보 다 넘겨주기
        User user = mapBasicInfo.getOwner();
        MapOwnerResponseDTO owner = MapOwnerResponseDTO.builder()
                .userId(user.getId())
                .imageUrl(user.getImage())
                .nickName(user.getNickname())
                .profileId(user.getProfileId())
                .amIFollowing(false)
                .build();

        return MapBasicInfoResponseDTO.builder()
                .mapId(mapBasicInfo.getMapId())
                .title(mapBasicInfo.getTitle())
                .address(mapBasicInfo.getAddress())
                .description(mapBasicInfo.getDescription())
                .latitude(mapBasicInfo.getLatitude())
                .longitude(mapBasicInfo.getLongitude())
                .isPublished(mapBasicInfo.isOnSearch())
                .isMine(false)
                .isBookmarked(false)
                .owner(owner)
                .build();
    }

    public void updateMapTitle(JwtUserDto jwtUserDto, long mapId, String updatedTitle) {
        log.info("MapService updateMapTitle - Retrieved map with id {}", mapId);

        Map map = mapRepository.findById(mapId);
        if (map == null) { new MapException(MapExceptionErrorCode.NO_EXIST_MAP);}

        map.setMapTitle(updatedTitle);
        mapRepository.save(map);
        log.info("MapService updateMapTitle - Updated title for map with id {} to '{}'", mapId, updatedTitle);
    }

    public void updateMapDescription(JwtUserDto jwtUserDto, long mapId, String updatedDescription) {
        log.info("MapService updateMapDescription - Retrieved map with id {}", mapId);

        Map map = mapRepository.findById(mapId);
        if (map == null) { new MapException(MapExceptionErrorCode.NO_EXIST_MAP);}

        map.setMapDescription(updatedDescription);
        mapRepository.save(map);
        log.info("MapService updateMapTitle - Updated title for map with id {} to '{}'", mapId, updatedDescription);
    }

    public void publishMap(JwtUserDto jwtUserDto, long mapId) {
        log.info("MapService publishMap - Retrieved map with id {}", mapId);
        Map map = mapRepository.findById(mapId);
        if (map == null) { new MapException(MapExceptionErrorCode.NO_EXIST_MAP);}
        map.setOnSearch(!map.isOnSearch());
        mapRepository.save(map);
    }

}
