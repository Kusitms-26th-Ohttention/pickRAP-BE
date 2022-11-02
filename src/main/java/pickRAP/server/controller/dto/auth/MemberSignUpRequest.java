package pickRAP.server.controller.dto.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class MemberSignUpRequest {

    private String email;

    private String password;

    private String name;
}
