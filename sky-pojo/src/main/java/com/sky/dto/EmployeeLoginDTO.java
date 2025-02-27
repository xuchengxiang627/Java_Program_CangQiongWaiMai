package com.sky.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "员工登录时传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @SchemaProperty(name = "用户名")
    private String username;

    @SchemaProperty(name = "密码")
    private String password;

}
