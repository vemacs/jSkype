package xyz.gghost.jskype.internal.packet;

import lombok.Data;

@Data
public class Header {
    private String type;
    private String data;

    public Header(String type, String data) {
        this.type = type;
        this.data = data;
    }
}
