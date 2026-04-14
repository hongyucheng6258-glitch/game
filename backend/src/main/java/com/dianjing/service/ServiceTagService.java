package com.dianjing.service;

import com.dianjing.dto.response.ServiceTagResponse;
import com.dianjing.entity.ServiceTag;

import java.util.List;

public interface ServiceTagService {

    ServiceTag create(String name);

    ServiceTag getById(Long id);

    ServiceTag update(Long id, String name);

    void delete(Long id);

    List<ServiceTag> getAll();

    List<ServiceTagResponse> getAllWithUseCount();

    long getUseCount(Long tagId);
}
