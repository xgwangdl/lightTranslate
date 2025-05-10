package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Xiehouyu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface XiehouyuRepository extends JpaRepository<Xiehouyu, Integer> {
    Page<Xiehouyu> findByRiddleContaining(String riddle, Pageable pageable);
}
