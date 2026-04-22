package com.backend.gamesales.Utils;
import java.util.Objects;

public final class Pagination {
    private static final int MAX_PAGE_SIZE = 100;

    private final int page;
    private final int size;
    public Pagination(int page, int size) {

        if (page < 0) {
            throw new RuntimeException("Page index cannot be negative");
        }
        if (size <= 0) {
            throw new RuntimeException("Page size must be greater than zero");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new RuntimeException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }
        this.page = page;
        this.size = size;
    }
    public int getPage() { return page; }
    public int getSize() { return size; }

    public static Pagination of(int page, int size) {
        return new Pagination(page, size);
    }
}
