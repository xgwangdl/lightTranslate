package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordBookRepository extends JpaRepository<WordBook, String> {
    WordBook findByBookId(String bookId);
}

