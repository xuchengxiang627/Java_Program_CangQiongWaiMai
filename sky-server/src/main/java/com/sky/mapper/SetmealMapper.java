package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 套餐
 */
@Mapper
public interface SetmealMapper extends BaseMapper<Setmeal> {

    IPage<SetmealVO> page(IPage<Setmeal> page, @Param("setmealPageQueryDTO") SetmealPageQueryDTO setmealPageQueryDTO);

}
