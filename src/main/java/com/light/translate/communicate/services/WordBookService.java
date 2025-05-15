package com.light.translate.communicate.services;

import com.light.translate.communicate.data.WordBook;
import com.light.translate.communicate.repository.WordBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WordBookService {

    private final WordBookRepository repository;


    public List<WordBook> getAllBooks() {
        return repository.findAll();
    }


    public WordBook getBookById(String bookId) {
        return repository.findByBookId(bookId);
    }

}