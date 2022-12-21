package pickRAP.server.controller.dto.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberSignInRequest {

    @NotEmpty
    private String email;

    @NotEmpty
    private String password;
}
