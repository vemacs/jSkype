package xyz.gghost.jskype.internal.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Header {
    private final String type;
    private final String data;
}
