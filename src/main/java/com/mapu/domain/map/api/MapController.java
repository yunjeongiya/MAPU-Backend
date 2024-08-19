package com.mapu.domain.map.api;

import com.mapu.domain.map.api.request.AddEditorRequestDTO;
import com.mapu.domain.map.api.request.CreateMapRequestDTO;
import com.mapu.domain.map.api.request.UpdateMapRequest;
import com.mapu.domain.map.application.MapService;
import com.mapu.domain.map.application.MapUserRoleService;
import com.mapu.domain.map.application.response.MapBasicInfoResponseDTO;
import com.mapu.domain.map.application.response.MapEditorListResponseDTO;
import com.mapu.domain.map.application.response.MapListResponseDTO;
import com.mapu.global.common.response.BaseResponse;
import com.mapu.global.jwt.dto.JwtUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {
    private final MapService mapService;
    private final MapUserRoleService mapUserRoleService;

    /**
     * 탐색화면 지도 리스트 조회 (랜덤 or 날짜)
     */
    @GetMapping("/search")
    public BaseResponse<List<MapListResponseDTO>> getMapList(@AuthenticationPrincipal JwtUserDto jwtUserDto,
            @RequestParam(value = "searchType", defaultValue = "RANDOM") String searchType,
            @RequestParam(value = "searchWord", defaultValue = "") String searchWord,
            final Pageable pageable) {

        //로그인 된 사용자의 경우
        if (jwtUserDto != null) {
            log.info("MapController getMapListforLoginedUser SearchType: {}, SearchWord: {}, Pageable: {}", searchType, searchWord, pageable);
            List<MapListResponseDTO> responseDTOList = mapService.getMapListForLoginedUser(jwtUserDto, searchType.toUpperCase(), pageable, searchWord);
            log.info("MapController getMapList - responseDTOList size: {}", responseDTOList.size());
            return new BaseResponse<>(responseDTOList);
        }
        log.info("MapController getMapListforAnaymousUser SearchType: {}, SearchWord: {}, Pageable: {}", searchType, searchWord, pageable);
        List<MapListResponseDTO> responseDTOList = mapService.getMapList(searchType.toUpperCase(), pageable, searchWord);
        log.info("MapController getMapList - responseDTOList size: {}", responseDTOList.size());
        return new BaseResponse<>(responseDTOList);
    }

    /**
     * 탐색화면 북마크 추가
     */
    @PostMapping("/bookmark")
    public BaseResponse addMapBookmark(@AuthenticationPrincipal JwtUserDto jwtUserDto, @RequestParam Long mapId) {
        mapService.addMapBookmark(Long.parseLong(jwtUserDto.getName()), mapId);
        return new BaseResponse<>();
    }

    /**
     * 탐색화면 북마크 취소
     */
    @DeleteMapping("/bookmark")
    public BaseResponse removeMapBookmark(@AuthenticationPrincipal JwtUserDto jwtUserDto, @RequestParam Long mapId) {
        mapService.removeMapBookmark(Long.parseLong(jwtUserDto.getName()), mapId);
        return new BaseResponse<>();
    }

    /**
     * 공동 편집자 목록 조회 API
     */
    @GetMapping("/{mapId}/editors")
    public BaseResponse<MapEditorListResponseDTO> getEditorList(@PathVariable("mapId") Long mapId,
                                                                @RequestParam("page") int pageNum,
                                                                @RequestParam("size") int pageSize) {
        MapEditorListResponseDTO response = mapUserRoleService.getEditorList(mapId,pageNum, pageSize);
        return new BaseResponse<>(response);
    }

    /**
     * 공동 편집자 추가 API
     */
    @PostMapping("/{mapId}/editor")
    public BaseResponse addEditor(@PathVariable("mapId") Long mapId,
                                  @RequestBody AddEditorRequestDTO addEditorRequestDTO){
        mapUserRoleService.addEditor(mapId, addEditorRequestDTO.getNickname());
        return new BaseResponse();
    }


    /**
     *  맵 생성
     */
    @PostMapping("/create")
    public BaseResponse<Void> createMap(@AuthenticationPrincipal JwtUserDto jwtUserDto,
                                                  @Valid @RequestBody CreateMapRequestDTO requestDTO) {
        Long userId = Long.parseLong(jwtUserDto.getName());
        mapService.createMap(requestDTO, userId);
        return new BaseResponse<>(null);
    }

    /**
     * 타유저의 지도데이터 조회
     */
    @GetMapping("/list/{otherUserId}")
    public BaseResponse getOtherUserMap(@PathVariable("otherUserId") long otherUserId,
                                        Pageable pageable) {
        List<MapListResponseDTO> response = mapService.getOtherUserMapList(otherUserId, pageable);
        return new BaseResponse<>(response);
    }

    /**
     * 지도 기본 정보 조회 API (지도 편집 화면 좌측 사이드 패널)
     */
    //edit인 경우 (access token을 받기 때문에 edit에만 사용할 수 있음)
    @GetMapping("/{mapId}/basic-info/edit")
    public BaseResponse<MapBasicInfoResponseDTO> getMapInfo(@AuthenticationPrincipal JwtUserDto jwtUserDto, @PathVariable("mapId") Long mapId){
        log.info("MapController getMapInfoForLoginedUser MapId: {}");
        //editor
        MapBasicInfoResponseDTO response = mapService.getMapBasicInfo(jwtUserDto, mapId);
        return new BaseResponse<>(response);
    }
    //edit인 경우 (access token을 받기 때문에 edit에만 사용할 수 있음)
    @GetMapping("/{mapId}/basic-info/view")
    public BaseResponse<MapBasicInfoResponseDTO> getMapInfo(@PathVariable("mapId") Long mapId){
        //viewer
        MapBasicInfoResponseDTO response = mapService.getMapBasicInfoForViewer(mapId);
        return new BaseResponse<>(response);
    }


    /**
     * 지도 제목 수정 API (지도 편집 화면 좌측 사이드 패널)
     */
    @Transactional
    @PatchMapping("/info/{mapId}/title")
    public BaseResponse updateMapTitle(@AuthenticationPrincipal JwtUserDto jwtUserDto,
                                       @PathVariable("mapId") long mapId,
                                       @RequestBody UpdateMapRequest updateMapRequest){
        mapService.updateMapTitle(jwtUserDto, mapId, updateMapRequest.getContent());
        return new BaseResponse();
    }

    /**
     * 지도 설명 수정 API (지도 편집 화면 좌측 사이드 패널)
     */
    @Transactional
    @PatchMapping("/info/{mapId}/description")
    public BaseResponse updateMapDescription(@AuthenticationPrincipal JwtUserDto jwtUserDto,
                                             @PathVariable("mapId") long mapId,
                                             @RequestBody UpdateMapRequest updateMapRequest){
        mapService.updateMapDescription(jwtUserDto, mapId, updateMapRequest.getContent());
        return new BaseResponse();
    }

    /**
     * 지도 게시하기
     */
    @PatchMapping("/{mapId}/publish")
    public BaseResponse publishMap (@AuthenticationPrincipal JwtUserDto jwtUserDto, @PathVariable("mapId") long mapId){
        mapService.publishMap(jwtUserDto,mapId);
        return new BaseResponse();
    }

}

