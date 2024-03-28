package com.api19_4.api19_4.dto;

import com.api19_4.api19_4.model.RgisterCertificate;
import com.api19_4.api19_4.models.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@Setter
public class SearchCertificateRequest implements Specification<RgisterCertificate> {
    private String cer_Id;
    private String cerName;
    private String status;


    @Override
    public Specification<RgisterCertificate> and(Specification<RgisterCertificate> other) {
        return Specification.super.and(other);
    }

    @Override
    public Specification<RgisterCertificate> or(Specification<RgisterCertificate> other) {
        return Specification.super.or(other);
    }

    @Override
    public Predicate toPredicate(Root<RgisterCertificate> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return null;
    }
}
