package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private CategoryMapper categoryMapper;


    @Override
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 插入套餐数据
        setmealMapper.insert(setmeal);

        log.info("新增套餐id: {}", setmeal.getId());

        // 插入套餐菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }


    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        IPage<Setmeal> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        IPage<SetmealVO> pageInfo = setmealMapper.page(page, setmealPageQueryDTO);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setRecords(pageInfo.getRecords());
        return pageResult;
    }

    @Override
    @Transactional
    public void deleteSetmeal(List<Long> ids) {
        // 起售中的套餐不能删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids)
                .eq(Setmeal::getStatus, StatusConstant.ENABLE);
        long cnt = setmealMapper.selectCount(queryWrapper);
        if (cnt > 0) {
            // 起售中的套餐不能删除
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE + ", ids=" + ids);
        }
        // 删除套餐数据
        setmealMapper.deleteBatchIds(ids);

        // 删除套餐菜品数据
        setmealDishMapper.delete(new LambdaUpdateWrapper<SetmealDish>().in(SetmealDish::getSetmealId, ids));
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        LambdaUpdateWrapper<Setmeal> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Setmeal::getId, id);
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(status);
        setmealMapper.update(setmeal, lambdaUpdateWrapper);
    }

    @Override
    public SetmealVO getSetmealById(Long id) {
        Setmeal setmeal = setmealMapper.selectById(id);
        if (setmeal == null) {
            throw new RuntimeException("套餐不存在");
        }
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);

        LambdaQueryWrapper<Category> categoryWrapper = new LambdaQueryWrapper<Category>();
        categoryWrapper.eq(Category::getId, setmeal.getCategoryId());
        Category category = categoryMapper.selectOne(categoryWrapper);
        setmealVO.setCategoryName(category.getName());

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, id)
                .orderByAsc(SetmealDish::getName);

        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);

        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void updateSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 更新菜套餐据
        setmealMapper.updateById(setmeal);

        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<SetmealDish>()
                .eq(SetmealDish::getSetmealId, setmealDTO.getId());
        // 删除原来的菜品数据
        setmealDishMapper.delete(wrapper);
        // 插入新的菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }
}
