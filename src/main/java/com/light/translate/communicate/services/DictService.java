package com.light.translate.communicate.services;

import com.light.translate.communicate.data.Dict;
import com.light.translate.communicate.repository.DictRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictService {

    @Autowired
    private DictRepository dictRepository;

    // 方案1的服务方法

    public String getDictValue(String dictType, String dictKey) {
        Dict dict = dictRepository.findByDictTypeAndDictKey(dictType, dictKey);
        return dict != null ? dict.getDictValue() : null;
    }

    public Map<String, String> getDictByType(String dictType) {
        return dictRepository.findByDictTypeOrderBySortAsc(dictType)
                .stream()
                .collect(Collectors.toMap(Dict::getDictKey, Dict::getDictValue));
    }
}