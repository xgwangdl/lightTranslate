package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Idioms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdiomsRepository extends JpaRepository<Idioms, Integer> {
    Page<Idioms> findByWordStartingWith(String keyword, Pageable pageable);
}
