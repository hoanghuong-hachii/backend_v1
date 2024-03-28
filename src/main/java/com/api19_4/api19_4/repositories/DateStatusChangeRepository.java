package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Bill;
import com.api19_4.api19_4.models.DateStatusChange;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface DateStatusChangeRepository extends JpaRepository<DateStatusChange, Long> {
    List<DateStatusChange> findAll(Specification<DateStatusChange> spec);
}
