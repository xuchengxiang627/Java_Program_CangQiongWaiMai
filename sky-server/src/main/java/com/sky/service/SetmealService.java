package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void addSetmeal(SetmealDTO setmealDTO);

    SetmealVO getSetmealById(Long id);

    void updateSetmeal(SetmealDTO setmealDTO);

    void deleteSetmeal(List<Long> ids);

    List<DishItemVO> getDishItemById(Long id);

}
