package com.scoks.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scoks.order.entity.OrderMaterialLog;
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
public interface OrderMaterialLogMapper extends BaseMapper<OrderMaterialLog> {

    List<OrderMaterialLog> listOrderMaterialLogs(Page<OrderMaterialLog> page, @Param("where") OrderMaterialLog form);
}
