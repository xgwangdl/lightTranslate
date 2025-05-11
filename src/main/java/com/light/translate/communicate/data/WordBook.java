package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "word_book")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordBook {

    @Id
    @Column(name = "book_id", length = 100)
    private String bookId;

    @Column(name = "book_name", nullable = false, length = 255)
    private String bookName;

    @Column(name = "word_count")
    private Integer wordCount;
}


