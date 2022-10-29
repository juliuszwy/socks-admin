package com.scoks.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scoks.order.dto.OrderQuery;
import com.scoks.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author julius
 * @since 2022-02-22
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    List<Order> listOrder(Page<Order> page, @Param("where") OrderQuery where);

    Order getOrder(Long id);

    int insertOrder(Order form);
}
