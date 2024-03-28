package com.api19_4.api19_4.repositories;

import com.api19_4.api19_4.models.Tutorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import javax.swing.*;
import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial,Long> {


    List<Tutorial> findByPublished(boolean published);
    List<Tutorial> findByTitleContaining(String title);

}
