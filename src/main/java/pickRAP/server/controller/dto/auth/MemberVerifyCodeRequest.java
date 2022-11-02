package pickRAP.server.controller.dto.auth;

import lombok.Data;

@Data
public class MemberVerifyCodeRequest {

    private String code;
}
