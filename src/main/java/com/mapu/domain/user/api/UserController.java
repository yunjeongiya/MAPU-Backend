package com.mapu.domain.user.api;

import com.mapu.domain.user.api.request.SignInRequestDTO;
import com.mapu.domain.user.api.request.SignUpRequestDTO;
import com.mapu.domain.user.api.request.UserUpdateRequestDTO;
import com.mapu.domain.user.application.UserService;
import com.mapu.domain.user.application.response.SignInUpResponseDTO;
import com.mapu.domain.user.application.response.UserInfoResponseDTO;
import com.mapu.domain.user.application.response.UserPageMapsResponseDTO;
import com.mapu.domain.user.exception.UserException;
import com.mapu.domain.user.exception.errorcode.UserExceptionErrorCode;
import com.mapu.global.common.response.BaseResponse;
import com.mapu.global.jwt.dto.JwtUserDto;
import com.mapu.infra.oauth.application.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final OAuthService oAuthService;

    /**
     * 유저 로그인 API
     */

    @PostMapping("/signin")
    public BaseResponse<SignInUpResponseDTO> socialLogin(@RequestBody SignInRequestDTO signInRequestDTO,
                                                         HttpServletRequest httpServletRequest,
                                                         HttpServletResponse httpServletResponse) {

        log.info("socialLoginType: {}", signInRequestDTO.getSocialType().toUpperCase());
        SignInUpResponseDTO response = oAuthService.login(signInRequestDTO.getSocialType().toUpperCase(), signInRequestDTO.getCode(), httpServletRequest.getSession(), httpServletResponse);

        return new BaseResponse<>(response);
    }

    /**
     * 유저 회원가입 API
     */

    @PostMapping("/signup")
    public BaseResponse<SignInUpResponseDTO> saveUser(@Validated @RequestPart("requestDTO") SignUpRequestDTO request,
                                                    @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
                                                    HttpServletRequest httpServletRequest,
                                                    HttpServletResponse httpServletResponse) throws IOException {
        SignInUpResponseDTO response = userService.signUp(request, imageFile, httpServletRequest.getSession(false), httpServletResponse);

        return new BaseResponse<>(response);
    }

    /**
     * 유저데이터 조회 API
     */

    @GetMapping
    public BaseResponse<UserInfoResponseDTO> getUserInfo(@AuthenticationPrincipal JwtUserDto jwtUserDto){
        UserInfoResponseDTO response = userService.getUserInfo(jwtUserDto);
        return new BaseResponse<>(response);
    }

    /**
     * 유저데이터 수정 API
     */
    @PatchMapping
    public BaseResponse updateUserInfo(@AuthenticationPrincipal JwtUserDto jwtUserDto,
                                       @Validated @RequestPart("requestDTO") UserUpdateRequestDTO request,
                                       @RequestPart(value = "imageFile", required = false)  MultipartFile image) throws IOException {
        userService.updateUser(Long.parseLong(jwtUserDto.getName()), request, image);
        return new BaseResponse<>();
    }

    /**
     * 유저데이터 삭제 API
     */
    @DeleteMapping
    public BaseResponse deleteUser(@AuthenticationPrincipal JwtUserDto jwtUserDto, HttpServletRequest httpServletRequest) {
        long deleteUserId = Long.parseLong(jwtUserDto.getName());
        userService.deleteUser(httpServletRequest, deleteUserId);
        oAuthService.unlinkUserInfo(deleteUserId);
        return new BaseResponse<>();
    }


    /**
     * 타유저데이터 조회 (지도 & 팔로우 & 팔로잉 정보)
     */
    @GetMapping("/{otherUserId}")
    public BaseResponse<UserInfoResponseDTO> getOtherUserInfo(@AuthenticationPrincipal JwtUserDto jwtUserDto, @PathVariable long otherUserId) {
        UserInfoResponseDTO response = userService.getOtherUserInfo(jwtUserDto, otherUserId);
        return new BaseResponse<>(response);
    }

    /**
     * 유저페이지 지도데이터 조회
     */
    @GetMapping("/maps")
    public BaseResponse<List<UserPageMapsResponseDTO>> getUserPageMaps(@AuthenticationPrincipal JwtUserDto jwtUserDto,
                                                                       @RequestParam(value = "editable", required = false) Boolean editable,
                                                                       @RequestParam(value = "bookmarked", required = false) Boolean bookmarked,
                                                                       @RequestParam(value = "search", required = false) String search){
        if (Boolean.TRUE.equals(editable) && Boolean.TRUE.equals(bookmarked)) {
            throw new UserException(UserExceptionErrorCode.DUPLICATE_CONDITION_IS_NOT_VALID);
        }

        if (editable==null || bookmarked==null){
            throw new UserException(UserExceptionErrorCode.NO_CONDITION_ERROR);
        }

        List<UserPageMapsResponseDTO> response = userService.getUserPageMaps(jwtUserDto,editable,bookmarked,search);

        return new BaseResponse<>(response);
    }
}
