package org.chainoptim.desktop.shared.table.util;

public interface StringConverter<T> {
    T convert(String input) throws Exception;
}
