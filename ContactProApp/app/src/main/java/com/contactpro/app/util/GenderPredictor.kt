package com.contactpro.app.util

object GenderPredictor {
    /**
     * Predictive gender detection has been disabled to ensure 100% accuracy.
     * All contacts will now default to "Prefer not to say" unless manually changed by the user.
     */
    fun predict(name: String?): String {
        return "Prefer not to say"
    }
}
