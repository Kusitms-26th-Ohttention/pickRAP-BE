package pickRAP.server.controller.dto.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class MemberSignInRequest {

    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
}
