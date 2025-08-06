package com.lseg.logmonitor;

public enum Status {
    START,
    END;


    public static Status fromString(String value) {
        return switch (value.strip().toUpperCase()) {
            case "START" -> START;
            case "END" -> END;
            default -> throw new IllegalArgumentException("Unknown status: " + value);
        };
    }
}
