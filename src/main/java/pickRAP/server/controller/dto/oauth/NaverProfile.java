package pickRAP.server.controller.dto.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverProfile {

    private String resultcode;

    private String message;

    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Response{
        private String id;
        private String email;
        private String name;
    }

}
