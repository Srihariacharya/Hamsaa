package com.contactpro.contactpro.util;

public class GenderPredictor {
    /**
     * Predictive gender detection has been disabled to ensure 100% accuracy.
     * All contacts will now default to "Prefer not to say" unless manually changed by the user.
     */
    public static String predict(String name) {
        return "Prefer not to say";
    }
}
