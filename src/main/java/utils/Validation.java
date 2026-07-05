package utils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class Validation {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s]{7,20}$");

    private Validation() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isBlank(email) && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return isBlank(phone) || PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidCgpa(BigDecimal cgpa) {
        return cgpa != null
            && cgpa.compareTo(BigDecimal.ZERO) >= 0
            && cgpa.compareTo(new BigDecimal("4.00")) <= 0;
    }
}
