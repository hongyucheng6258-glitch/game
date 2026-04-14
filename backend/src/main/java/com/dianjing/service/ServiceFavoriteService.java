package com.dianjing.service;

import com.dianjing.entity.ServiceFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceFavoriteService {

    void favorite(Long userId, Long serviceId);

    void unfavorite(Long userId, Long serviceId);

    boolean isFavorited(Long userId, Long serviceId);

    Page<ServiceFavorite> list(Long userId, Pageable pageable);

    long count(Long userId);
}
