package pickRAP.server.config.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import pickRAP.server.common.BaseResponse;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static pickRAP.server.common.BaseExceptionStatus.*;


@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper objectMapper = new ObjectMapper();
        String errorJson = objectMapper.writeValueAsString(new BaseResponse<>(UN_AUTHORIZED));
        response.getWriter().print(errorJson);
    }
}
