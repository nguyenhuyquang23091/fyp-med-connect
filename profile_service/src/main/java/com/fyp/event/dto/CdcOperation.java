package com.fyp.event.dto;

public enum CdcOperation {
    CREATE("c"),

    READ("r"),

    UPDATE("u"),

    DELETE("d");

    private final String code;

    CdcOperation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static CdcOperation fromCode(String code) {
        for (CdcOperation op : values()) {
            if (op.code.equals(code)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown CDC operation code: " + code);
    }
}
