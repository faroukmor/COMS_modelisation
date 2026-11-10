package com.charity.view;

/** Entry for drop-down lists: shows a readable label but carries the record id. */
public record Item(Long id, String label) {
    @Override
    public String toString() {
        return label;
    }
}
