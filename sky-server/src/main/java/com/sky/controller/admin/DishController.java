package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.RedisConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Tag(name = "菜品管理接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;


    @PostMapping
    @Operation(summary = "新增菜品")
    public Result<String> addDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品");
        dishService.addDish(dishDTO);

        redisTemplate.delete(RedisConstant.DISH_CACHE_PRE + dishDTO.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "菜品分页查询")
    public Result<Object> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询");
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @Operation(summary = "批量删除菜品")
    public Result<String> deleteDish(@RequestParam("ids") List<Long> ids) {
        log.info("批量删除菜品");
        dishService.deleteDish(ids);

        Set keys = redisTemplate.keys(RedisConstant.DISH_CACHE_PRE + "*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "菜品起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品起售停售");
        dishService.startOrStop(status, id);

        Set keys = redisTemplate.keys(RedisConstant.DISH_CACHE_PRE + "*");
        redisTemplate.delete(keys);

        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品");
        DishVO dishVO = dishService.getDishById(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @Operation(summary = "修改菜品")
    public Result<String> updateDish(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品");
        dishService.updateDish(dishDTO);

        String pattern = RedisConstant.DISH_CACHE_PRE + "*";
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        log.info("根据分类id查询菜品");
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, categoryId);
        List<Dish> dishList = dishService.list(queryWrapper);
        return Result.success(dishList);
    }



}
