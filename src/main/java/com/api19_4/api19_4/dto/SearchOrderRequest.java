package com.api19_4.api19_4.dto;

import com.api19_4.api19_4.models.Orderr;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class SearchOrderRequest implements Specification<Orderr> {
    @Override
    public Specification<Orderr> and(Specification<Orderr> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<Orderr> or(Specification<Orderr> other) {
        return Specification.super.or(other);
    }

    @Override
    public Predicate toPredicate(Root<Orderr> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }
}
