package pickRAP.server.config.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
public class TokenDto {

    private String accessToken;

    private String refreshToken;

}
