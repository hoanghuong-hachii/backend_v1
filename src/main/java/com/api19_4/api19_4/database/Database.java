package com.api19_4.api19_4.database;

import com.api19_4.api19_4.models.Product;
import com.api19_4.api19_4.repositories.ProductRepository;
import com.api19_4.api19_4.repositories.UserRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//Now connect with sql server using JPA
@Configuration
public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
//                Product product1 = new Product( "MacBook", 2023, 2400.0, "");
//                Product product2 = new Product("Dell", 2023, 500.0, "");
//                logger.info("insert data" + productRepository.save(product1));
//                logger.info("insert data" + productRepository.save(product2));
            }
        };
    }
    CommandLineRunner initDatabase(UserRepositories userRepositories){
        return new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {

            }
        };
    }
}
