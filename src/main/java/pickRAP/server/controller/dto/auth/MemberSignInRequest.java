package pickRAP.server.controller.dto.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MemberSignInRequest {

    private String email;

    private String password;
}
