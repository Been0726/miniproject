package com.example.miniproject.repository;

import com.example.miniproject.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;
import java.util.List;

public interface ArticleRepository extends CrudRepository<Article, Long> {

    @Override
    ArrayList<Article> findAll();

    List<Article> findAllByOrderByIdDesc();

    Page<Article> findByCreatedBy(String email, Pageable pageable);

}
