package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.SceneTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SceneThemeRepository extends JpaRepository<SceneTheme, Long> {

    // 1. 获取所有启用状态的数据，按sort_order排序
    List<SceneTheme> findByStatusOrderBySortOrderAsc(Integer status);

    // 2. 随机获取三个启用状态的数据（使用原生SQL）
    @Query(value = "SELECT * FROM scene_theme WHERE status = :status ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<SceneTheme> findRandomThreeByStatus(@Param("status") Integer status);
}
