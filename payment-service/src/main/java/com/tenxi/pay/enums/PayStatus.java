package com.tenxi.pay.enums;

import lombok.Data;

public enum PayStatus {
    PENDING(0),
    SUCCESS(1),
    FAILED(-1),
    REFUNDED(-2);

    private final int value;

    PayStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
