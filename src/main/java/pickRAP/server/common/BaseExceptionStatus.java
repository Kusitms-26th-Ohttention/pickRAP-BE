package pickRAP.server.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BaseExceptionStatus {

    // 성공
    SUCCESS(HttpStatus.OK, 1000, "요청 성공"),
    // 인증
    UN_AUTHORIZED(HttpStatus.UNAUTHORIZED, 2000, "토큰 검증 실패"),
    SC_FORBIDDEN(HttpStatus.FORBIDDEN, 2001, "권한 없음"),
    INVALID_EMAIL(HttpStatus.BAD_REQUEST, 2002, "이메일 형식을 확인해주세요"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, 2003, "비밀번호 형식을 확인해주세요"),
    EXIST_ACCOUNT(HttpStatus.BAD_REQUEST, 2004, "이미 존재하는 회원입니다"),
    FAIL_LOGIN(HttpStatus.BAD_REQUEST, 2005, "로그인 실패"),
    EMPTY_INPUT_VALUE(HttpStatus.BAD_REQUEST, 2006, "값을 모두 입력해주세요"),

    // 회원


    // 스크랩
    NOT_SUPPORT_FILE(HttpStatus.BAD_REQUEST, 4001, "지원하지 않는 파일 형식입니다"),
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, 4002, "파일 업로드 실패"),
    FILE_DOWNLOAD_FAIL(HttpStatus.BAD_REQUEST, 4003, "파일 다운로드 실패"),
    EXIST_CATEGORY(HttpStatus.BAD_REQUEST, 4004, "이미 존재하는 카테고리입니다"),
    SAME_CATEGORY(HttpStatus.BAD_REQUEST, 4005, "변경하려는 카테고리의 이름이 같습니다"),
    DONT_EXIST_CATEGORY(HttpStatus.BAD_REQUEST, 4006, "카테고리가 존재하지 않습니다"),
    SCRAP_TITLE_LONG(HttpStatus.BAD_REQUEST, 4007, "제목이 15자를 초과했습니다"),
    DONT_EXIST_FILE(HttpStatus.BAD_REQUEST, 4008, "파일이 존재하지 않습니다"),
    DONT_EXIST_SCRAP(HttpStatus.BAD_REQUEST, 4009, "스크랩이 존재하지 않습니다"),
    CANT_DELETE_CATE(HttpStatus.BAD_REQUEST, 4010, "기본 카테고리는 삭제할 수 없습니다"),
    CATEGORY_TITLE_LONG(HttpStatus.BAD_REQUEST, 4011, "제목이 20자를 초과했습니다"),
    DONT_EXIST_KEYWORD(HttpStatus.BAD_REQUEST, 4012, "검색어를 입력해주세요"),
    DONT_EXIST_PATH(HttpStatus.BAD_REQUEST, 4013, "경로가 잘못되었습니다"),
    DONT_EXIST_CONTENT(HttpStatus.BAD_REQUEST, 4014, "컨텐츠를 입력해주세요"),

    // 메거진
    EXCEED_PAGE_SIZE(HttpStatus.BAD_REQUEST, 5001, "매거진 페이지는 최대 20장까지 제작 가능합니다."),
    EXCEED_TEXT_LENGTH(HttpStatus.BAD_REQUEST, 5002, "매거진 텍스트는 최대 1000자까지 입력 가능합니다."),
    NOT_MATCH_WRITER(HttpStatus.BAD_REQUEST, 5003, "작성자가 일치하지 않습니다."),
    NOT_SELECTED_ELEMENT(HttpStatus.BAD_REQUEST, 5004, "삭제할 항목을 선택해주세요.");

    // 분석&추천

    // 커뮤니티


    private HttpStatus httpStatus;
    private final int code;
    private final String message;

    private BaseExceptionStatus(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
