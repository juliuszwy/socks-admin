package com.scoks.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scoks.order.Constant;
import com.scoks.order.entity.Material;
import com.scoks.order.entity.StorageLog;
import com.scoks.order.entity.StorageRegionMaterial;
import com.scoks.order.exception.Result;
import com.scoks.order.service.StorageServiceImpl;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scoks/api/storage")
public class StorageController extends BaseController {

    @Autowired
    private StorageServiceImpl storageService;

    @GetMapping("material")
    @RequiresAuthentication
    public Object listMaterial(Page<Material> page, Material form) throws Exception {
        return storageService.findMaterialPageList(page, form);
    }

    @RequiresAuthentication
    @RequiresRoles(value = {Constant.POSITION_BOSS, Constant.STORAGE}, logical = Logical.OR)
    @PostMapping(value = "/material_edit")
    public Result material_edit(Material dict) {
        storageService.setMaterial(dict);
        return Result.success(null);
    }

    @RequiresAuthentication
    @RequiresRoles(value = {Constant.POSITION_BOSS, Constant.STORAGE}, logical = Logical.OR)
    @PostMapping(value = "/material_delete")
    public Result material_delete(Material dict) {
        dict.setState(1);
        storageService.setMaterial(dict);
        return Result.success(null);
    }

    @GetMapping(value = "/material_sum")
    public Object sum_material(Page<StorageRegionMaterial> page, StorageRegionMaterial from) {
        return storageService.findStorageMaterialPageList(page, from);
    }

    @GetMapping(value = "/material_sum_detail")
    public Object material_sum_detail(Page<StorageLog> page, StorageLog from) {
        return storageService.findStorageLogPageList(page, from);
    }

    @PostMapping(value = "/material_in_out")
    public Result material_in_out(@RequestBody List<StorageLog> forms) {
        for (StorageLog form : forms) {
            storageService.insetStorageLog(form);
        }
        return Result.success(null);
    }
}
