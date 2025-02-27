package com.sky.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工登录返回的数据格式")
public class EmployeeLoginVO implements Serializable {

    @SchemaProperty(name = "主键值")
    private Long id;

    @SchemaProperty(name = "用户名")
    private String userName;

    @SchemaProperty(name = "姓名")
    private String name;

    @SchemaProperty(name = "jwt令牌")
    private String token;

}
