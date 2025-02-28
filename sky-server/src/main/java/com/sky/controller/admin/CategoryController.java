package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Tag(name = "员工相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @PutMapping
    @Operation(summary = "修改分类")
    public Result<String> updateCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类：{}", categoryDTO);
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询分类")
    public Result<PageResult> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询分类：{}", categoryPageQueryDTO);
        PageResult page = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(page);
    }

    @PostMapping("/status/{status}")
    @Operation(summary = "启用禁用分类")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用禁用分类：{}, {}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    @PostMapping
    @Operation(summary = "新增分类")
    public Result<String> addCategory(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO);
        categoryService.addCategory(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "删除分类")
    public Result<String> deleteCategory(Long id) {
        log.info("删除分类：{}", id);
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "根据类型查询分类")
    public Result<List<Category>> list(Integer type) {
        log.info("根据类型查询分类：{}", type);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getType, type);
        queryWrapper.orderByAsc(Category::getSort);
        return Result.success(categoryService.list(queryWrapper));
    }
}
