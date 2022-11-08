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
public class OrderMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;
    private Long orderId;
    /**
     * 原料id
     */
    private Long materialId;

    /**
     * 所需原料数
     */
    private Long targetNum;

    /**
     * 已经领取
     */
    private Long getNum;

    private Long creator;

    private Long createTime;

    private Long updateTime;


    @TableField(exist = false)
    private Integer materialCategory;
    @TableField(exist = false)
    private String materialName;
    @TableField(exist = false)
    private String materialColour;
    @TableField(exist = false)
    private String materialManufactor;
}
