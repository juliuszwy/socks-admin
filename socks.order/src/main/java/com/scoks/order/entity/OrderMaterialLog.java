package com.scoks.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author julius
 * @since 2022-11-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class OrderMaterialLog extends BaseEntity {

    private static final long serialVersionUID = 1L;
    /**
     * 原料id
     */
    private Long orderMaterialId;

    /**
     * 已经领取
     */
    private Long getNum;

    private Long creator;

    private Long createTime;

    private Long updateTime;


    @TableField(exist = false)
    private String creatorName;

}
