package de.adesso.objectfieldcoverage.api.assertion.primitive.numeric;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Interval<S extends Number> {

    private S lowerBound;

    private S upperBound;

}
