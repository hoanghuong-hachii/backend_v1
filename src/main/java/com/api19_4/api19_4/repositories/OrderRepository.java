package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Orderr;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
@Transactional
public interface OrderRepository extends JpaRepository<Orderr, String>, JpaSpecificationExecutor<Orderr> {
    List<Orderr> findByIdUser(Long idUser);
    List<Orderr> findByDateOrder(Date dateOrder);
    @Query("SELECT COUNT(o) FROM Orderr o")
    int countAllOrderrs();
}
