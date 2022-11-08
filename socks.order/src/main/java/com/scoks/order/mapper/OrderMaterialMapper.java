package com.scoks.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scoks.order.entity.OrderMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author julius
 * @since 2022-11-08
 */
@Mapper
public interface OrderMaterialMapper extends BaseMapper<OrderMaterial> {

    List<OrderMaterial> listOrderMaterial(Page<OrderMaterial> page, @Param("where") OrderMaterial form);

    void updateGetNum(Long id, Long getNum);
}
