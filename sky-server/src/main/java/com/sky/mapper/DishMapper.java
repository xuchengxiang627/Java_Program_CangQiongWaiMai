package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 菜品
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    IPage<DishVO> pageQuery(IPage<DishVO> page, @Param("dishPageQueryDTO") DishPageQueryDTO dishPageQueryDTO);
}
