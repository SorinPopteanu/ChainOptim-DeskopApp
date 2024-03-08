package org.chainoptim.desktop.shared.search.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class PaginatedResults<T> {
    public List<T> results;
    public long totalCount;
}

