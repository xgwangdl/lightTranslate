package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Example;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExampleRepository extends JpaRepository<Example, Long> {
    List<Example> findByWordId(String wordId);
}

