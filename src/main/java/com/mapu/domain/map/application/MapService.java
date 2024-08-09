package com.mapu.domain.map.application;

import com.mapu.domain.map.api.request.CreateMapRequestDTO;
import com.mapu.domain.map.application.response.MapListResponseDTO;
import com.mapu.domain.map.application.response.MapOwnerResponseDTO;
import com.mapu.domain.map.dao.MapKeywordRepository;
import com.mapu.domain.map.dao.MapRespository;
import com.mapu.domain.map.domain.Map;
import com.mapu.domain.map.exception.MapException;
import com.mapu.domain.map.exception.errcode.MapExceptionErrorCode;
import com.mapu.domain.user.dao.UserRepository;
import com.mapu.domain.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MapService {

    private  MapRespository mapRepository;
    private MapKeywordRepository keywordRepository;
    private UserRepository userRepository;

    public void checkLoginStatus(HttpServletRequest request) {
    }

    public List<MapListResponseDTO> getMapList(String searchType, int page, int rows) {
        switch (searchType) {
            case "RANDOM": {
                List<MapListResponseDTO> mapList = GetMapListByRandom(page, rows);
                return mapList;
            }
            case "DATE": {
                List<MapListResponseDTO> mapList = GetMapListByDate(page, rows);
                return mapList;
            }
            default:
                new MapException(MapExceptionErrorCode.SOCIALTYPE_ERROR);
        }
        return null;
    }

    private List<MapListResponseDTO> GetMapListByDate(int page, int rows) {
        Pageable pageable = (Pageable) PageRequest.of(page, rows);
        List<Map> maps = mapRepository.findAllByOrderByCreatedAtDesc(pageable);
        log.info("MapService GetMapListByDate - Retrieved {} map(s) from the database", maps.size());
        return maps.stream().map(this::mapConvertToDTO).collect(Collectors.toList());
    }

    private List<MapListResponseDTO> GetMapListByRandom(int page, int rows) {
        Pageable pageable = (Pageable) PageRequest.of(page, rows);
        // TODO : Pageable 오류 해결 (제대로 paging 처리가 안돼)
        List<Map> maps = mapRepository.findAllByRandom(pageable);
        log.info("MapService GetMapListByRandom - Retrieved {} map(s) from the database", maps.size());
        return maps.stream().map(this::mapConvertToDTO).collect(Collectors.toList());
    }

    private MapListResponseDTO mapConvertToDTO(Map map) {

        MapOwnerResponseDTO mapOwnerDTO = null;
        User user = map.getUser();
        if(user != null) {
            mapOwnerDTO = MapOwnerResponseDTO.builder()
                    .imageUrl(user.getImage())
                    .nickName(user.getNickname())
                    .profileId(user.getProfileId())
                    .build();
        }

        List<String> keywords = keywordRepository.findKeywordsByMapId(map.getId());

        return MapListResponseDTO.builder()
                .title(map.getMapTitle())
                .region(map.getAddress())
                .description(map.getMapDescription())
                .imageUrl(map.getPublishLink())
                .user(mapOwnerDTO)
                .keyword(keywords)
                .build();
    }

    public void createMap(CreateMapRequestDTO requestDTO, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("유저 없음"));

        Map map = Map.builder()
                .mapTitle(requestDTO.getMapTitle())
                .mapDescription(requestDTO.getMapDescription())
                .address(requestDTO.getAddress())
                .latitude(requestDTO.getLatitude())
                .longitude(requestDTO.getLongitude())
                .zoomLevel(requestDTO.getZoomLevel())
                .publishLink(requestDTO.getPublishLink())
                .isOnSearch(requestDTO.getIsOnSearch())
                .user(user)
                .build();
        mapRepository.save(map);
    }

}
