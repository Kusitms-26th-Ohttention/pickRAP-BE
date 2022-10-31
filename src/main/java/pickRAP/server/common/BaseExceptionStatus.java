package pickRAP.server.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseExceptionStatus {

    // 성공
    SUCCESS(1000, "요청 성공"),
    // 인증
    UN_AUTHORIZED(2000, "토큰 검증 실패"),
    SC_FORBIDDEN(2001, "권한 없음");

    // 회원


    // 스크랩


    // 메거진


    // 분석&추천


    private final int code;
    private final String message;

    private BaseExceptionStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
