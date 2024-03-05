package org.chainoptim.desktop.shared.search.model;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class PaginatedResults<T> {
    List<T> results;
    long totalCount;
}
