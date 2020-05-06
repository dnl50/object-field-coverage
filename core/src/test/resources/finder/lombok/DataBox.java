package de.adesso.test;

import lombok.Data;

@Data
public class DataBox {

    private int width;

    private int height;

    private int depth;

    private boolean empty;

    private Boolean full;

    // overrides lombok generated public getter
    protected int getWidth() {
        return width;
    }

}
