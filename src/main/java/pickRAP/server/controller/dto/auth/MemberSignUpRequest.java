package pickRAP.server.controller.dto.auth;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MemberSignUpRequest {

    @NotEmpty
    private String email;

    @NotEmpty
    private String password;

    @NotEmpty
    private String name;

}
