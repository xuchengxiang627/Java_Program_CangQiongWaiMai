package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Tag(name = "套餐管理接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @Operation(summary = "新增套餐")
    public Result<String> addSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐");
        setmealService.addSetmeal(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "套餐分页查询")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询");
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @Operation(summary = "批量删除套餐")
    public Result<String> deleteSetmeal(@RequestParam("ids") List<Long> ids) {
        log.info("批量删除套餐");
        if (ids == null || ids.size() == 0) {
            return Result.error("ids不能为空");
        }
        setmealService.deleteSetmeal(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "套餐起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("套餐起售停售");
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("根据id查询套餐");
        SetmealVO setmealVO = setmealService.getSetmealById(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @Operation(summary = "修改套餐")
    public Result<String> updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐");
        setmealService.updateSetmeal(setmealDTO);
        return Result.success();
    }


}
