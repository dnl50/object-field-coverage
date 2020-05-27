package de.adesso.test;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class DataClassExtendingDataClass {

    protected boolean exists;

    protected short length;

    @EqualsAndHashCode.Exclude
    protected int excludedInt;

}
