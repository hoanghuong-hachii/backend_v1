package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.ImageProductAvatar;
import com.api19_4.api19_4.models.ImageProductDetail;
import org.hibernate.query.criteria.JpaSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ImageRepositoryDetail extends JpaRepository<ImageProductDetail, Long>, JpaSpecificationExecutor<ImageProductDetail> {
    List<ImageProductDetail> findByIdProd(Long idProd);

}
