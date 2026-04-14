package com.dianjing.service;

import com.dianjing.entity.Service;

import java.util.List;

public interface ServiceCacheService {
    void cacheServiceDetail(Service service);
    Service getCachedServiceDetail(Long serviceId);
    void evictServiceDetail(Long serviceId);
    void cacheRecommendedServices(String gameType, List<Service> services);
    List<Service> getCachedRecommendedServices(String gameType);
    void evictRecommendedServices(String gameType);
    void evictAllServiceCaches();
}
