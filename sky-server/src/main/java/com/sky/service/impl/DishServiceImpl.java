package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Override
    @Transactional
    public void addDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 插入菜品数据
        dishMapper.insert(dish);

        log.info("新增菜品id: {}", dish.getId());

        // 插入菜品口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }


    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        IPage<DishVO> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        IPage<DishVO> pageInfo = dishMapper.pageQuery(page, dishPageQueryDTO);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setRecords(pageInfo.getRecords());
        return pageResult;
    }

    @Override
    @Transactional
    public void deleteDish(List<Long> ids) {
        // 起售中的菜品不能删除，关联套餐的菜品不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.selectById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                // 起售中的菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE + ", 菜品id=" + id + ", 菜品名称=" + dish.getName());
            }

            LambdaUpdateWrapper<SetmealDish> queryWrapper = new LambdaUpdateWrapper<>();
            queryWrapper.eq(SetmealDish::getDishId, id);
            if (setmealDishMapper.selectCount(queryWrapper) > 0) {
                // 关联套餐的菜品不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL + ", 菜品id=" + id + ", 菜品名称=" + dish.getName());
            }
        }
        // 删除菜品数据
        dishMapper.deleteBatchIds(ids);

        // 删除菜品口味数据
        dishFlavorMapper.delete(new LambdaUpdateWrapper<DishFlavor>().in(DishFlavor::getDishId, ids));
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        LambdaUpdateWrapper<Dish> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Dish::getId, id);
        Dish dish = new Dish();
        dish.setStatus(status);
        dishMapper.update(dish, lambdaUpdateWrapper);
    }

    @Override
    public DishVO getDishById(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorMapper.selectList(wrapper);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 更新菜品数据
        dishMapper.updateById(dish);

        LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<DishFlavor>()
                .eq(DishFlavor::getDishId, dishDTO.getId());
        // 删除原来的口味数据
        dishFlavorMapper.delete(wrapper);
        // 插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }

        // 图片？
    }
}
