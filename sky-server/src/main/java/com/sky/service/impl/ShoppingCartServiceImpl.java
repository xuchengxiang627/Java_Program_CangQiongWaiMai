package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 判断当前菜品或套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .eq(shoppingCartDTO.getDishId() != null, ShoppingCart::getDishId, shoppingCartDTO.getDishId())
                .eq(shoppingCartDTO.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor())
                .eq(shoppingCartDTO.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId());

        ShoppingCart shoppingCart = shoppingCartMapper.selectOne(queryWrapper);
        // 存在，则数量加一
        if (shoppingCart != null) {
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.updateById(shoppingCart);
            return;
        }
        // 不存在，则添加到购物车
        shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .dishId(shoppingCartDTO.getDishId())
                .dishFlavor(shoppingCartDTO.getDishFlavor())
                .setmealId(shoppingCartDTO.getSetmealId())
                .number(1)
                .createTime(LocalDateTime.now())
                .build();
        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            Setmeal setmeal = setmealMapper.selectById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .eq(shoppingCartDTO.getDishId() != null, ShoppingCart::getDishId, shoppingCartDTO.getDishId())
                .eq(shoppingCartDTO.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCartDTO.getDishFlavor())
                .eq(shoppingCartDTO.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCartDTO.getSetmealId());

        ShoppingCart shoppingCart = shoppingCartMapper.selectOne(queryWrapper);
        // 存在，则数量加一
        if (shoppingCart.getNumber() == 1) {
            shoppingCartMapper.deleteById(shoppingCart);
            return;
        }
        // 不存在，则添加到购物车
        shoppingCart.setNumber(shoppingCart.getNumber() - 1);
        shoppingCartMapper.updateById(shoppingCart);
    }
}
