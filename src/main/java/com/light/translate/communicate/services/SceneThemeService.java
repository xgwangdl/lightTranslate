package com.light.translate.communicate.services;

import com.light.translate.communicate.data.SceneTheme;
import com.light.translate.communicate.repository.SceneThemeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SceneThemeService {

    private final SceneThemeRepository repository;

    public SceneThemeService(SceneThemeRepository repository) {
        this.repository = repository;
    }

    // 获取所有启用的，按排序权重排序
    public List<SceneTheme> getAllEnabledSorted() {
        return repository.findByStatusOrderBySortOrderAsc(1);
    }

    // 随机获取三个启用的
    public List<SceneTheme> getRandomThreeEnabled() {
        return repository.findRandomThreeByStatus(1);
    }
}

