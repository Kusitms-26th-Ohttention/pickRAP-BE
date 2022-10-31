package pickRAP.server.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import static pickRAP.server.common.BaseExceptionStatus.*;

@Data
public class BaseResponse<T> {

    private int code;

    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    // data 있는 경우
    public BaseResponse(T data) {
        this.code = SUCCESS.getCode();
        this.message = SUCCESS.getMessage();
        this.data = data;
    }

    // data 없는 경우
    public BaseResponse(BaseExceptionStatus status) {
        this.code = status.getCode();
        this.message = status.getMessage();
    }
}
