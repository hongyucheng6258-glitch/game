package com.dianjing.util;

import java.util.regex.Pattern;

public class PasswordUtil {

    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public static int calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int strength = 0;

        if (password.length() >= 6) {
            strength += 1;
        }
        if (password.length() >= 8) {
            strength += 1;
        }
        if (password.length() >= 12) {
            strength += 1;
        }

        if (LOWERCASE_PATTERN.matcher(password).find()) {
            strength += 1;
        }
        if (UPPERCASE_PATTERN.matcher(password).find()) {
            strength += 1;
        }
        if (DIGIT_PATTERN.matcher(password).find()) {
            strength += 1;
        }
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            strength += 1;
        }

        return Math.min(strength, 5);
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }

        boolean hasLower = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasUpper = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecial = SPECIAL_CHAR_PATTERN.matcher(password).find();

        int typeCount = 0;
        if (hasLower) typeCount++;
        if (hasUpper) typeCount++;
        if (hasDigit) typeCount++;
        if (hasSpecial) typeCount++;

        return typeCount >= 2;
    }

    public static String getPasswordStrengthLabel(int strength) {
        return switch (strength) {
            case 0 -> "非常弱";
            case 1 -> "弱";
            case 2 -> "一般";
            case 3 -> "较强";
            case 4 -> "强";
            case 5 -> "非常强";
            default -> "未知";
        };
    }
}
