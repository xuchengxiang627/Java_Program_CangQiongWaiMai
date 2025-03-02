package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 菜品
 */
@Mapper
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
    void insertBatch(List<DishFlavor> flavors);
}
