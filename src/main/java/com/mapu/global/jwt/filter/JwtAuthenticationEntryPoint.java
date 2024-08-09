package com.mapu.global.jwt.filter;

import com.mapu.global.common.response.BaseErrorResponse;
import com.mapu.global.jwt.exception.errorcode.JwtExceptionErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final static String EXCEPTION = "exception";
    private final static String TOKEN_MALFORMED = "malformed";
    private final static String TOKEN_EXPIRED = "expired";
    private final static String TOKEN_NULL = "null";
    private final static String BEARER = "bearer";
    private final static String TOKEN_INVALID = "invalid";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String)request.getAttribute(EXCEPTION);
        response.setContentType("application/json; charset=UTF-8");

        if(exception!=null) {
            if(exception.equals(TOKEN_EXPIRED)) {
                response.getWriter().write(
                        new BaseErrorResponse(JwtExceptionErrorCode.EXPIRED_JWT_TOKEN)
                                .convertToJson()
                );
            } if (exception.equals(TOKEN_MALFORMED)) {
                response.getWriter().write(
                        new BaseErrorResponse(JwtExceptionErrorCode.MALFORMED_JWT_TOKEN)
                                .convertToJson()
                );
            } if(exception.equals(TOKEN_NULL)) {
                response.getWriter().write(
                        new BaseErrorResponse(JwtExceptionErrorCode.NO_JWT_TOKEN_IN_HEADER)
                                .convertToJson()
                );
            } if(exception.equals(BEARER)) {
                response.getWriter().write(
                        new BaseErrorResponse(JwtExceptionErrorCode.NO_BEARER_TYPE)
                                .convertToJson()
                );
            } if(exception.equals(TOKEN_INVALID)) {
                response.getWriter().write(
                        new BaseErrorResponse(JwtExceptionErrorCode.INVALID_JWT_TOKEN)
                                .convertToJson()
                );
            }
        }
    }
}
