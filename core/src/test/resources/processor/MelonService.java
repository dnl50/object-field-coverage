package de.adesso.test;

import de.adesso.test.Melon;
import java.util.List;

public class MelonService {

    public void deleteMelons(List<Melon> melons) {
        // implementation here
    }

    public void deleteMelons(Melon[] melons) {
        // implementation here
    }

    public Melon saveMelon(Melon melon) {
        return melon;
    }

    public <T> void unboundGenericMethod(T obj) {
        // method body here
    }

    public <T extends Number> void boundGenericMethod(Number number) {
        // method body here
    }

}
