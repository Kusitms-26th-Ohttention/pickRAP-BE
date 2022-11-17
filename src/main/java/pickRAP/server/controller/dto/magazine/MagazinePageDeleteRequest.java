package pickRAP.server.controller.dto.magazine;

import lombok.Data;

import java.util.List;

@Data
public class MagazinePageDeleteRequest {
    List<Long> pages;
}
