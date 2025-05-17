package com.light.translate.communicate.services;

import com.light.translate.communicate.data.UserCollection;
import com.light.translate.communicate.dto.UserCollectionDTO;
import com.light.translate.communicate.repository.UserCollectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserWordCollectService {

    @Autowired
    private UserCollectRepository repository;

    public boolean checkCollected(String openid, String wordId) {
        return repository.existsByOpenidAndWordId(openid, wordId);
    }

    public void addCollect(String openid, String wordId, String category) {
        if (!repository.existsByOpenidAndWordId(openid, wordId)) {
            UserCollection collect = new UserCollection();
            collect.setOpenid(openid);
            collect.setWordId(wordId);
            collect.setCategory(category);
            repository.save(collect);
        }
    }

    public void cancelCollect(String openid, String wordId) {
        UserCollection userCollection = repository.findByOpenidAndWordId(openid, wordId);
        repository.delete(userCollection);
    }

    public Page<UserCollectionDTO> listCollects(String openid, Pageable pageable) {
        return repository.findAllByOpenid(openid,pageable);
    }
    public Page<UserCollectionDTO> listCollects(String openid, String category, Pageable pageable) {
        return repository.findByOpenidAndCategory(openid,category,pageable);
    }
}

