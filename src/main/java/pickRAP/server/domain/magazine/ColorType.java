package pickRAP.server.domain.magazine;

import lombok.Getter;

@Getter
public enum ColorType {
    RED("화려한 레드"),
    BLUE("시원한 블루"),
    LEMON("따뜻한 레몬"),
    MINT("맑은 민트"),
    PINK("상큼한 핑크"),
    VIOLET("오묘한 바이올렛"),
    ORANGE("포근한 오렌지"),
    GREEN("깔끔한 그린"),
    NAVY("차가운 네이비"),
    PURPLE("잔잔한 퍼플"),
    BLACK("모던한 블랙"),
    PEACH("산뜻한 피치");

    private final String value;

    private ColorType(String value) {
        this.value = value;
    }

    public static ColorType from(String value) {
        for (ColorType type : ColorType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

}
