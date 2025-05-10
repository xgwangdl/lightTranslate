package com.light.translate.communicate.services;

import com.light.translate.communicate.data.CiWord;
import com.light.translate.communicate.repository.CiWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CiWordService {
    @Autowired
    private CiWordRepository repository;

    public Page<CiWord> search(String ci, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ci == null || ci.isEmpty()
                ? repository.findAll(pageable)
                : repository.findByCiStartingWith(ci, pageable);
    }

    public CiWord getById(Integer id) {
        return repository.findById(id).orElse(null);
    }
}
