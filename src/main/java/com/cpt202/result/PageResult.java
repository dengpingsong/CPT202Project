package com.cpt202.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResult <T> {
    private Long total;
    private List<T> records;
}
