package pickRAP.server.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class BaseException extends RuntimeException{

    private BaseExceptionStatus status;

}
