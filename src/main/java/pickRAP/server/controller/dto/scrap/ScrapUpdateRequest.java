package pickRAP.server.controller.dto.scrap;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScrapUpdateRequest {

    private Long id;

    private String title;

    private String memo;

    private List<String> hashtags;
}
