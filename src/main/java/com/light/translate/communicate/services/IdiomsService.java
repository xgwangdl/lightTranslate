package com.light.translate.communicate.services;

import com.light.translate.communicate.data.Idioms;
import com.light.translate.communicate.repository.IdiomsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class IdiomsService {

    @Autowired
    private IdiomsRepository idiomsRepository;

    public Page<Idioms> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (keyword == null || keyword.isEmpty()) {
            return idiomsRepository.findAll(pageable);
        } else {
            return idiomsRepository.findByWordStartingWith(keyword, pageable);
        }
    }

    public Idioms getById(Integer id) {
        return idiomsRepository.findById(id).orElse(null);
    }
}
