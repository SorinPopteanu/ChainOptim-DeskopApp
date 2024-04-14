package org.chainoptim.desktop.shared.enums;

public enum InfoLevel {
    NONE,
    ADVANCED,
    ALL;

    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
