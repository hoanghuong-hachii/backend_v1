package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.models.ShoppingCart;
import com.api19_4.api19_4.models.ShoppingCartId;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart,String>, JpaSpecificationExecutor<ShoppingCart> {
    @Override
    List<ShoppingCart> findAll();

    @Override
    Optional<ShoppingCart> findById(String id);

    Optional<ShoppingCart> findByIdUserAndIdProd(String idUser, String idProd);

    List<ShoppingCart> findByIdUser(String idUser);

    @Query("SELECT COUNT(sc) FROM ShoppingCart sc")
    int countAllShoppingCart();

    ShoppingCart findTopByOrderByIdShCartDesc();
}
