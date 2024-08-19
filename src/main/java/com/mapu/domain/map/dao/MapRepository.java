package com.mapu.domain.map.dao;

import com.mapu.domain.map.application.response.MapBasicInfoDTO;
import com.mapu.domain.map.application.response.MapBasicInfoResponseDTO;
import com.mapu.domain.map.application.response.MapListResponseDTO;
import com.mapu.domain.user.application.response.UserPageMapsDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.mapu.domain.map.domain.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface MapRepository extends JpaRepository<Map, Long> {
    @Query("SELECT m FROM Map m WHERE LOWER(m.mapTitle) LIKE LOWER(CONCAT('%', :searchWord, '%')) AND m.isOnSearch=true ORDER BY m.created_at DESC")
    List<Map> findAllByOrderByCreatedAtDesc(@Param("searchWord") String searchWord, Pageable pageable);

    @Query("SELECT m FROM Map m WHERE LOWER(m.mapTitle) LIKE LOWER(CONCAT('%', :searchWord, '%')) AND m.isOnSearch=true ORDER BY FUNCTION('RAND')")
    List<Map> findAllByRandom(@Param("searchWord") String searchWord, Pageable pageable);

    @Query("SELECT m FROM Map m WHERE LOWER(m.mapTitle) LIKE LOWER(CONCAT('%', :searchWord, '%')) AND m.isOnSearch=true AND m.user.id != :userId ORDER BY m.created_at DESC")
    List<Map> findAllByOrderByCreatedAtDescForLoginedUser(@Param("searchWord") String searchWord, @Param("userId") long userId, Pageable pageable);

    @Query("SELECT m FROM Map m WHERE LOWER(m.mapTitle) LIKE LOWER(CONCAT('%', :searchWord, '%')) AND m.isOnSearch=true AND m.user.id != :userId ORDER BY FUNCTION('RAND')")
    List<Map> findAllByRandomForLoginedUser(@Param("searchWord") String searchWord, @Param("userId") long userId, Pageable pageable);

    Map findById(long mapId);

    @Query("SELECT COUNT(m) FROM Map m WHERE m.user.id = :userId")
    int countMapsByUserId(@Param("userId") Long userId);

    @Query("SELECT m FROM Map m WHERE m.user.id = :otherUserId AND m.isOnSearch = true")
    List<Map> findOtherUserMapsByUserId(@Param("otherUserId") Long otherUserId, Pageable pageable);

    List<Map> findByUserId(Long userId);

    @Query("SELECT m FROM Map m JOIN m.keywords k WHERE k.keyword.keyword = :keyword")
    List<Map> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT new com.mapu.domain.user.application.response.UserPageMapsDTO(" +
            "m.id, m.imageUrl, m.mapTitle, m.address, mur.role, m.created_at, m.longitude, m.latitude) " +
            "FROM MapUserRole mur " +
            "JOIN mur.map m " +
            "WHERE mur.user.id = :userId " +
            "AND (:search IS NULL OR LOWER(m.mapTitle) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<UserPageMapsDTO> findEditableMaps(@Param("userId") Long userId, @Param("search") String search);

    @Query("SELECT new com.mapu.domain.user.application.response.UserPageMapsDTO(" +
            "m.id, m.imageUrl, m.mapTitle, m.address, mur.role, " +
            "m.created_at, m.longitude, m.latitude) " +
            "FROM MapUserBookmark mub " +
            "JOIN mub.map m " +
            "JOIN m.user u " +
            "LEFT JOIN MapUserRole mur ON mur.map.id = m.id AND mur.user.id = :userId " +
            "WHERE mub.user.id = :userId " +
            "AND (:search IS NULL OR LOWER(m.mapTitle) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<UserPageMapsDTO> findBookmarkedMaps(@Param("userId") Long userId, @Param("search") String search);

    @Query("select new com.mapu.domain.map.application.response.MapBasicInfoDTO( " +
            "m.id, m.mapTitle, m.address, m.mapDescription, m.latitude, m.longitude, m.isOnSearch, u " +
            ") FROM Map m JOIN User u ON m.user = u AND m.id = :mapId ")
    MapBasicInfoDTO findMapBasicInfoById(@Param("mapId") long mapId);
}
