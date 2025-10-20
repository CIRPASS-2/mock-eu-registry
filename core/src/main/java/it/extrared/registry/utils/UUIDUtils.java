package it.extrared.registry.utils;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

/** UUID related utils methods. */
public class UUIDUtils {

    private static final TimeBasedGenerator TIME_GENERATOR = Generators.timeBasedGenerator();

    /**
     * @return a time based UUID.
     */
    public static String generateTimeBasedUUID() {
        return TIME_GENERATOR.generate().toString();
    }
}
