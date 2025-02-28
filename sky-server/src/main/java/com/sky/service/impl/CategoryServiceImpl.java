package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.UpdateFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        int i = categoryMapper.updateById(category);
        if (i == 0) {
            throw new UpdateFailedException("修改分类失败");
        }
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        IPage<Category> page = new Page<>(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        LambdaQueryWrapper<Category> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (categoryPageQueryDTO.getName() != null && !categoryPageQueryDTO.getName().trim().isEmpty()) {
            lambdaQueryWrapper.like(Category::getName, categoryPageQueryDTO.getName());
        }
        if (categoryPageQueryDTO.getType() != null) {
            lambdaQueryWrapper.eq(Category::getType, categoryPageQueryDTO.getType());
        }
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        IPage<Category> pageInfo = categoryMapper.selectPage(page, lambdaQueryWrapper);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setRecords(pageInfo.getRecords());
        return pageResult;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        LambdaUpdateWrapper<Category> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(Category::getId, id);
        lambdaUpdateWrapper.set(Category::getStatus, status);
        lambdaUpdateWrapper.set(Category::getUpdateUser, BaseContext.getCurrentId());
        lambdaUpdateWrapper.set(Category::getUpdateTime, LocalDateTime.now());
        int update = categoryMapper.update(null, lambdaUpdateWrapper);
        if (update == 0) {
            throw new UpdateFailedException("分类状态修改失败");
        }

    }

    @Override
    public void addCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setId(null);
        category.setStatus(StatusConstant.DISABLE);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        categoryMapper.insert(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 判断当前分类是否关联了菜品，如果关联了，抛出业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        long count = dishMapper.selectCount(dishLambdaQueryWrapper);
        if (count > 0) {
            // 当前分类下有菜品，不能删除
            throw new DeletionNotAllowedException("当前分类下有菜品，不能删除");
        }

        // 判断当前分类是否关联了套餐，如果关联了，抛出业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        count = setmealMapper.selectCount(setmealLambdaQueryWrapper);
        if (count > 0) {
            // 当前分类下有套餐，不能删除
            throw new DeletionNotAllowedException("当前分类下有套餐，不能删除");
        }

        int i = categoryMapper.deleteById(id);
        if (i == 0) {
            throw new UpdateFailedException("删除分类失败");
        }

    }

    @Override
    public void testAop() {
        Category category = Category.builder().status(1).sort(10086).name("a222opppp").build();
        categoryMapper.testAop(category);
    }


}
