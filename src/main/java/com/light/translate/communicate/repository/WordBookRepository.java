package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordBookRepository extends JpaRepository<WordBook, String> {
    WordBook findByBookId(String bookId);
    List<WordBook> findAllById(Iterable<String> bookIds);
}

