package pickRAP.server.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pickRAP.server.common.BaseResponse;
import pickRAP.server.service.s3.S3Service;

import java.io.IOException;
import java.util.List;

import static pickRAP.server.common.BaseExceptionStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {
    private final S3Service s3Service;

    @PostMapping("/upload")
    @ApiOperation(value = "파일 업로드", notes = "단일 파일 업로드")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4001-지원하지않는파일, 4002-업로드실패"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String msg = s3Service.uploadFile(multipartFile, "content");

        BaseResponse baseResponse = new BaseResponse(SUCCESS);
        baseResponse.setMessage(msg);

        return ResponseEntity.ok(baseResponse);
    }

    @PostMapping("/uploads")
    @ApiOperation(value = "파일 업로드", notes = "다중 파일 업로드")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4001-지원하지않는파일, 4002-업로드실패"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<BaseResponse> uploadFiles(@RequestParam("file") List<MultipartFile> multipartFiles) throws IOException {
        s3Service.uploadFiles(multipartFiles, "content");

        return ResponseEntity.ok(new BaseResponse(SUCCESS));
    }

    @GetMapping("/download/{fileName}")
    @ApiOperation(value = "파일 다운로드", notes = "프론트로 파일 전송 시에는 http 헤더에 Content-Type 넣고 해당 파일 보내주기")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "4003-다운로드실패"),
            @ApiResponse(responseCode = "500", description = "서버 예외")
    })
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) throws Exception {
        return s3Service.downloadFile(fileName, "content");
    }
}
