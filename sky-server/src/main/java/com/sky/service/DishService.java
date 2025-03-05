package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService extends IService<Dish> {


    void addDish(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteDish(List<Long> ids);

    void startOrStop(Integer status, Long id);

    DishVO getDishById(Long id);

    void updateDish(DishDTO dishDTO);

    List<DishVO> listWithFlavor(Dish dish);

}
