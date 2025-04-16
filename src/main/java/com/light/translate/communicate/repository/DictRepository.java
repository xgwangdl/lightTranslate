package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Dict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {
    List<Dict> findByDictTypeOrderBySortAsc(String dictType);

    Dict findByDictTypeAndDictKey(String dictType, String dictKey);
}