package pickRAP.server.controller.dto.category;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDeleteRequest {

    private List<Long> id;
}
