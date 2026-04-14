package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.entity.ServiceFavorite;
import com.dianjing.mapper.ServiceFavoriteMapper;
import com.dianjing.mapper.ServiceMapper;
import com.dianjing.service.ServiceFavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceFavoriteServiceImpl implements ServiceFavoriteService {

    private final ServiceFavoriteMapper serviceFavoriteMapper;
    private final ServiceMapper serviceMapper;

    public ServiceFavoriteServiceImpl(ServiceFavoriteMapper serviceFavoriteMapper, ServiceMapper serviceMapper) {
        this.serviceFavoriteMapper = serviceFavoriteMapper;
        this.serviceMapper = serviceMapper;
    }

    @Override
    @Transactional
    public void favorite(Long userId, Long serviceId) {
        if (!serviceMapper.existsById(serviceId)) {
            throw new BusinessException(404, "服务不存在");
        }
        if (serviceFavoriteMapper.existsByUserIdAndServiceId(userId, serviceId)) {
            throw new BusinessException(400, "已收藏该服务");
        }
        ServiceFavorite favorite = new ServiceFavorite();
        favorite.setUserId(userId);
        favorite.setServiceId(serviceId);
        serviceFavoriteMapper.save(favorite);
    }

    @Override
    @Transactional
    public void unfavorite(Long userId, Long serviceId) {
        if (!serviceFavoriteMapper.existsByUserIdAndServiceId(userId, serviceId)) {
            throw new BusinessException(400, "未收藏该服务");
        }
        serviceFavoriteMapper.deleteByUserIdAndServiceId(userId, serviceId);
    }

    @Override
    public boolean isFavorited(Long userId, Long serviceId) {
        return serviceFavoriteMapper.existsByUserIdAndServiceId(userId, serviceId);
    }

    @Override
    public Page<ServiceFavorite> list(Long userId, Pageable pageable) {
        return serviceFavoriteMapper.findByUserIdOrderByIdDesc(userId, pageable);
    }

    @Override
    public long count(Long userId) {
        return serviceFavoriteMapper.countByUserId(userId);
    }
}
