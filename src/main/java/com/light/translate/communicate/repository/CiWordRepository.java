package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.CiWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CiWordRepository extends JpaRepository<CiWord, Integer> {
    Page<CiWord> findByCiStartingWith(String ci, Pageable pageable);
}
