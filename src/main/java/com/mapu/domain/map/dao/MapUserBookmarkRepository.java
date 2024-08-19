package com.mapu.domain.map.dao;

import com.mapu.domain.map.domain.MapUserBookmark;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MapUserBookmarkRepository extends JpaRepository<MapUserBookmark, Long> {
    MapUserBookmark findByUserIdAndMapId(Long userId, Long mapId);

    @Query("select count(mub)>0 " +
            "from MapUserBookmark mub " +
            "join User u on u.id=:userId " +
            "join Map m on m.id=:mapId")
    boolean isExistByUserIdAndMapId(@Param("userId") Long userId, @Param("mapId") Long mapId);

    boolean existsByMapIdAndUserId(Long mapId, long userId);
}
