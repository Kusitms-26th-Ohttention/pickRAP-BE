package pickRAP.server.controller.dto.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryRequest {

    private String name;
}
