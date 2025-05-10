package com.light.translate.communicate.services;

import com.light.translate.communicate.data.Example;
import com.light.translate.communicate.repository.ExampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExampleService {

    @Autowired
    private ExampleRepository exampleRepository;

    public List<Example> getExamplesByWordId(String wordId) {
        return exampleRepository.findByWordId(wordId);
    }
}

