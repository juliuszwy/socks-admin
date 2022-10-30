package com.scoks.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 仓库区域原料统计
 * </p>
 *
 * @author julius
 * @since 2022-10-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class StorageRegionMaterial extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 区域
     */
    private String region;

    /**
     * 原料id
     */
    private Long materialId;

    /**
     * 货物总量
     */
    private Long sum;

    /**
     * 备注
     */
    private String remarks;

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
