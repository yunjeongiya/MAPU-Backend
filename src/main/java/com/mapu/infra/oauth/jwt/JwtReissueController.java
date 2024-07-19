package com.mapu.infra.oauth.jwt;

import com.mapu.global.common.response.BaseResponse;
import com.mapu.infra.oauth.jwt.dto.JwtUserDto;
import com.mapu.infra.oauth.jwt.dto.ReissueResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.mapu.infra.oauth.jwt.JwtUtil.REFRESH;

@RequiredArgsConstructor
@RestController
public class JwtReissueController {
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    @PostMapping("/reissue")
    public BaseResponse<ReissueResponseDto> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(REFRESH)) {
                refresh = cookie.getValue();
            }
        }

        JwtUserDto jwtUserDto = jwtService.getUserDtoFromToken(refresh, REFRESH);
        response.addCookie(jwtUtil.createAccessJwtCookie(jwtUserDto));
        //TODO refresh rotation

        return new BaseResponse<>(new ReissueResponseDto()); //TODO 응답 뭐 넣을지 채우기
    }
}
