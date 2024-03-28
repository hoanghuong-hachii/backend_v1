package com.api19_4.api19_4.util;

import com.api19_4.api19_4.enums.SortOrderEnum;
import com.api19_4.api19_4.models.Constants;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SearchUtil {
    public static Pageable getPageableFromParam(Integer page, Integer size, String sort, SortOrderEnum order) {
        Sort sortRequest;
        if (sort != null) {
            sortRequest = Sort.by(order == SortOrderEnum.asc ? Sort.Direction.ASC : Sort.Direction.DESC, sort);
        } else {
            sortRequest = Sort.unsorted();
        }
        if (size == null || size > Constants.DEFAULT_PAGE_SIZE_MAX) {
            size = Constants.DEFAULT_PAGE_SIZE_MAX;
        }
        return PageRequest.of(page, size, sortRequest);
    }
    public static Pageable getPageableFromParamP(Integer page, Integer size) {
        if (size == null || size > Constants.DEFAULT_PAGE_SIZE_MAX) {
            size = Constants.DEFAULT_PAGE_SIZE_MAX;
        }
        return PageRequest.of(page, size);
    }
// price
//    public static Pageable getPageableFromParamP(Integer page, Integer size, String sort, Float priceFrom, Float priceTo) {
//        Sort sortRequest;
//        if(sort != null){
//            sortRequest = Sort.by(ge("price", priceFrom), le("price", priceTo));
//        }
//        if (size == null || size > Constants.DEFAULT_PAGE_SIZE_MAX) {
//            size = Constants.DEFAULT_PAGE_SIZE_MAX;
//        }
//        return PageRequest.of(page, size, sort);
//    }

    public static <T> Specification<T> like(String fieldName, String value) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (value != null) {
                    return cb.like(cb.lower(root.get(fieldName)), value.toLowerCase());
                }
                return cb.conjunction();
            }
        };
    }

    public static <R, F> Specification<R> in(String fieldName, List<F> filterList) {
        return new Specification<R>() {
            @Override
            public Predicate toPredicate(Root<R> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (filterList != null && !filterList.isEmpty()) {
                    if (filterList.size() > 1) {
                        CriteriaBuilder.In<F> inClause = cb.in(root.get(fieldName));
                        filterList.forEach(e -> inClause.value(e));
                        return inClause;
                    } else {
                        return cb.equal(root.<Comparable>get(fieldName), filterList.get(0));
                    }
                }
                return cb.conjunction();
            }
        };
    }

    public static <T> Specification<T> eq(String fieldName, Object value) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (value != null) {
                    return cb.equal(root.<Comparable>get(fieldName), value);
                }
                return cb.conjunction();
            }
        };
    }

    public static <T> Specification<T> gt(String fieldName, Comparable value) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (value != null) {
                    cb.greaterThan(root.<Comparable>get(fieldName), value);
                }
                return cb.conjunction();
            }
        };
    }

    public static <T> Specification<T> ge(String fieldName, Comparable value) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (value != null) {
                    return cb.greaterThanOrEqualTo(root.<Comparable>get(fieldName), value);
                }
                return cb.conjunction();
            }
        };
    }

    public static <T> Specification<T> lt(String fieldName, Comparable value) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (value != null) {
                    return cb.lessThan(root.<Comparable>get(fieldName), value);
                }
                return cb.conjunction();
            }
        };
    }

    public static <T> Specification<T> le(String fieldName, Comparable value) {
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (value != null) {
                    cb.lessThanOrEqualTo(root.<Comparable>get(fieldName), value);
                }
                return cb.conjunction();
            }
        };
    }


}

