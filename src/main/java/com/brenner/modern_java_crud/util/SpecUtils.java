package com.brenner.modern_java_crud.util;

import java.util.Collection;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SpecUtils {

    public static <T, R, V> Specification<T> joinIn(
        final String relation,
        final String field,
        final Collection<V> values
    ) {
        return (root, query, cb) -> values == null || values.isEmpty() ? null
            : root.join(relation).get(field).in(values);
    }

    public static <T, V> Specification<T> in(
        final String field,
        final Collection<V> values
    ) {
        return (root, query, cb) -> values == null || values.isEmpty() ? null
            : root.get(field).in(values);
    }

    public static <T, V> Specification<T> equals(
        final String field,
        final V value
    ) {
        return (root, query, cb) -> value == null ? null
            : cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> likeIgnoreCase(
        final String field,
        final String value
    ) {
        return (root, query, cb) -> StringUtils.hasText(value) ? cb
            .like(cb.lower(root.get(field)), "%" + value.toLowerCase() + "%")
            : null;
    }

    public static <T, V extends Comparable<V>> Specification<T> greaterOrEqual(
        final String field,
        final V value
    ) {
        return (root, query, cb) -> value == null ? null
            : cb.greaterThanOrEqualTo(root.get(field), value);
    }

    public static <T, V extends Comparable<V>> Specification<T> lessOrEqual(
        final String field,
        final V value
    ) {
        return (root, query, cb) -> value == null ? null
            : cb.lessThanOrEqualTo(root.get(field), value);
    }

}
