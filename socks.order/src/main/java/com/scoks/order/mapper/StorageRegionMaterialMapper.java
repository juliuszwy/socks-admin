package com.scoks.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scoks.order.entity.StorageRegionMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 仓库区域原料统计 Mapper 接口
 * </p>
 *
 * @author julius
 * @since 2022-10-30
 */
@Mapper
public interface StorageRegionMaterialMapper extends BaseMapper<StorageRegionMaterial> {
    List<StorageRegionMaterial> storageRegionMaterials(Page<StorageRegionMaterial> page, @Param("where") StorageRegionMaterial form);

    int updateNum(Long storageRegionId, Long num);
}
