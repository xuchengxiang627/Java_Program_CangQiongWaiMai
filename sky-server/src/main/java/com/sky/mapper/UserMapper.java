package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import com.sky.temPojo.NewUser;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 菜品
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    List<NewUser> selectNewUser(Map<String, LocalDateTime> map);

}
