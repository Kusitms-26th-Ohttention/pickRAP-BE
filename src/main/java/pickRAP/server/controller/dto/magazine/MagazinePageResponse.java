package pickRAP.server.controller.dto.magazine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class MagazinePageResponse {
    @JsonProperty(value="page_id")
    private Long pageId;
    @JsonProperty(value="file_url")
    private String fileUrl;
    @JsonProperty(value = "preview_url")
    private String previewUrl;
    private String contents;
    private String text;

    @Builder
    public MagazinePageResponse(Long pageId, String fileUrl, String previewUrl, String contents, String text) {
        this.pageId = pageId;
        this.fileUrl = fileUrl;
        this.previewUrl = previewUrl;
        this.contents = contents;
        this.text = text;
    }

}
