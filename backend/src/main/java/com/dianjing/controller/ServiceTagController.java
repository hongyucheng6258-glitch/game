package com.dianjing.controller;

import com.dianjing.common.Result;
import com.dianjing.dto.response.ServiceTagResponse;
import com.dianjing.entity.ServiceTag;
import com.dianjing.service.ServiceTagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/service-tags", "/api/v1/tags"})
public class ServiceTagController {

    private final ServiceTagService serviceTagService;

    public ServiceTagController(ServiceTagService serviceTagService) {
        this.serviceTagService = serviceTagService;
    }

    @GetMapping
    public Result<List<ServiceTagResponse>> list() {
        return Result.success(serviceTagService.getAllWithUseCount());
    }
}
