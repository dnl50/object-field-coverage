package de.adesso.test;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Getter
public class TypeAndFieldGetterBox {

    private int width;

    @Getter(AccessLevel.PROTECTED)
    private int height;

    @Getter(AccessLevel.PRIVATE)
    private int depth;

}
