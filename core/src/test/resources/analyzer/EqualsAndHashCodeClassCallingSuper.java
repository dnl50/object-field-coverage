package de.adesso.test;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(
        callSuper = true,
        onlyExplicitlyIncluded = true
)
public class EqualsAndHashCodeClassCallingSuper extends DataClassExtendingDataClass {

    @EqualsAndHashCode.Include
    private Object explicitlyIncluded;

    private Object notExplicitlyIncluded;

}
