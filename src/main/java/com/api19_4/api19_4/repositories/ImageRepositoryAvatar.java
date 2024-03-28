package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.ImageProductAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface ImageRepositoryAvatar extends JpaRepository<ImageProductAvatar, Long>, JpaSpecificationExecutor<ImageProductAvatar> {
    List<ImageProductAvatar> findByImageName(String ImageName);
    List<ImageProductAvatar> findByIdProd(Long idProd);
}
