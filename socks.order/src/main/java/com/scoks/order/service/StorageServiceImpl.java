package com.scoks.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scoks.order.entity.Material;
import com.scoks.order.entity.Staff;
import com.scoks.order.entity.StorageLog;
import com.scoks.order.entity.StorageRegionMaterial;
import com.scoks.order.exception.ResultException;
import com.scoks.order.exception.ResultStatus;
import com.scoks.order.mapper.MaterialMapper;
import com.scoks.order.mapper.StorageLogMapper;
import com.scoks.order.mapper.StorageRegionMaterialMapper;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class StorageServiceImpl {
    @Resource
    private MaterialMapper materialMapper;
    @Resource
    private StorageLogMapper storageLogMapper;
    @Resource
    private StorageRegionMaterialMapper storageRegionMaterialMapper;


    public Page<Material> findMaterialPageList(Page<Material> page, Material form) {
        List<Material> materials = materialMapper.listMaterials(page, form);
        page.setRecords(materials);
        return page;
    }

    public void setMaterial(Material form) {
        if (form.getId() == null) {//新增
            long l = System.currentTimeMillis();
            form.setCreateTime(l);
            form.setUpdateTime(l);
            form.setState(0);
            form.setSum(0L);
            materialMapper.insert(form);
        } else {
            form.setUpdateTime(System.currentTimeMillis());
            materialMapper.updateById(form);
        }
    }


    public Page<StorageRegionMaterial> findStorageMaterialPageList(Page<StorageRegionMaterial> page, StorageRegionMaterial form) {
        List<StorageRegionMaterial> materials = storageRegionMaterialMapper.storageRegionMaterials(page, form);
        page.setRecords(materials);
        return page;
    }

    public Page<StorageLog> findStorageLogPageList(Page<StorageLog> page, StorageLog form) {
        List<StorageLog> materials = storageLogMapper.listStorageLog(page, form);
        page.setRecords(materials);
        return page;
    }

    @Transactional
    public void insetStorageLog(StorageLog form) {
        long now = System.currentTimeMillis();
        String region = form.getRegion();
        Long materialId = form.getMaterialId();
        QueryWrapper<StorageRegionMaterial> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("region", region);
        objectQueryWrapper.eq("material_id", materialId);
        Long storageRegionId = null;
        StorageRegionMaterial db = storageRegionMaterialMapper.selectOne(objectQueryWrapper);
        if (db == null) {
            StorageRegionMaterial storageRegionMaterial = new StorageRegionMaterial();
            storageRegionMaterial.setMaterialId(materialId);
            storageRegionMaterial.setRegion(region);
            storageRegionMaterial.setCreateTime(now);
            storageRegionMaterial.setUpdateTime(now);
            storageRegionMaterialMapper.insert(storageRegionMaterial);
            storageRegionId = storageRegionMaterial.getId();
        } else {
            storageRegionId = db.getId();
        }

        form.setStorageRegionId(storageRegionId);
        Subject currentUser = SecurityUtils.getSubject();
        Staff user = (Staff) currentUser.getPrincipal();
        form.setCreator(user.getId());
        form.setCreateTime(now);
        form.setUpdateTime(now);
        storageLogMapper.insert(form);

        int row = storageRegionMaterialMapper.updateNum(storageRegionId, form.getNum());
        if (row > 0) {
            materialMapper.updateNum(materialId, form.getNum());
        } else {
            throw new ResultException(ResultStatus.MATERIAL_NOT_ENOUGH);
        }
    }
}
