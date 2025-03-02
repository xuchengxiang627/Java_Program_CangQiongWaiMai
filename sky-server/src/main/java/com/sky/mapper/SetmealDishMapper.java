package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 套餐
 */
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
    void insertBatch(List<SetmealDish> setmealDishes);


}
