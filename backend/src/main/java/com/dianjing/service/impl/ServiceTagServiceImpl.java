package com.dianjing.service.impl;

import com.dianjing.common.BusinessException;
import com.dianjing.dto.response.ServiceTagResponse;
import com.dianjing.entity.ServiceTag;
import com.dianjing.mapper.ServiceTagMapper;
import com.dianjing.mapper.ServiceTagRelationMapper;
import com.dianjing.service.ServiceTagService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceTagServiceImpl implements ServiceTagService {

    private final ServiceTagMapper serviceTagMapper;
    private final ServiceTagRelationMapper serviceTagRelationMapper;

    public ServiceTagServiceImpl(ServiceTagMapper serviceTagMapper, ServiceTagRelationMapper serviceTagRelationMapper) {
        this.serviceTagMapper = serviceTagMapper;
        this.serviceTagRelationMapper = serviceTagRelationMapper;
    }

    @Override
    public ServiceTag create(String name) {
        if (serviceTagMapper.existsByName(name)) {
            throw new BusinessException(400, "标签名称已存在");
        }
        ServiceTag tag = new ServiceTag();
        tag.setName(name);
        return serviceTagMapper.save(tag);
    }

    @Override
    public ServiceTag getById(Long id) {
        return serviceTagMapper.findById(id)
                .orElseThrow(() -> new BusinessException(404, "标签不存在"));
    }

    @Override
    public ServiceTag update(Long id, String name) {
        ServiceTag tag = getById(id);
        if (serviceTagMapper.existsByName(name) && !tag.getName().equals(name)) {
            throw new BusinessException(400, "标签名称已存在");
        }
        tag.setName(name);
        return serviceTagMapper.save(tag);
    }

    @Override
    public void delete(Long id) {
        if (!serviceTagMapper.existsById(id)) {
            throw new BusinessException(404, "标签不存在");
        }
        long useCount = getUseCount(id);
        if (useCount > 0) {
            throw new BusinessException(400, "该标签已被 " + useCount + " 个服务使用，无法删除");
        }
        serviceTagMapper.deleteById(id);
    }

    @Override
    public List<ServiceTag> getAll() {
        return serviceTagMapper.findAllByOrderByNameAsc();
    }

    @Override
    public List<ServiceTagResponse> getAllWithUseCount() {
        List<ServiceTag> tags = getAll();
        return tags.stream()
                .map(tag -> ServiceTagResponse.fromEntity(tag, getUseCount(tag.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public long getUseCount(Long tagId) {
        return serviceTagRelationMapper.countByTagId(tagId);
    }
}
