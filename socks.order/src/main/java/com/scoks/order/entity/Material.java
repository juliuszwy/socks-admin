package com.scoks.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 原料
 * </p>
 *
 * @author julius
 * @since 2022-10-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class Material extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 类别
     */
    private Integer category;

    /**
     * 名称
     */
    private String name;

    /**
     * 颜色编号
     */
    private String colour;

    /**
     * 厂家
     */
    private String manufactor;

    /**
     * 总数
     */
    private Long sum;

    private Integer state;

    private Long createTime;

    private Long updateTime;


}
