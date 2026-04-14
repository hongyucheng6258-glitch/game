package com.dianjing.controller.admin;

import com.dianjing.common.Result;
import com.dianjing.entity.ServiceTag;
import com.dianjing.service.ServiceTagService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/tags")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final ServiceTagService serviceTagService;

    public AdminTagController(ServiceTagService serviceTagService) {
        this.serviceTagService = serviceTagService;
    }

    @GetMapping
    public Result<List<ServiceTag>> list() {
        return Result.success(serviceTagService.getAll());
    }

    @PostMapping
    public Result<ServiceTag> create(@RequestBody Map<String, String> data) {
        String name = data.get("name");
        return Result.success(serviceTagService.create(name));
    }

    @PutMapping("/{id}")
    public Result<ServiceTag> update(@PathVariable Long id, @RequestBody Map<String, String> data) {
        String name = data.get("name");
        return Result.success(serviceTagService.update(id, name));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceTagService.delete(id);
        return Result.success(null);
    }
}
