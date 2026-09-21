package com.supermarket.mapper;

import com.supermarket.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户表 Mapper
 */
public interface UserMapper {

    /** 新增用户 */
    int insert(User user);

    /** 按用户名查询 */
    User selectByUsername(@Param("username") String username);

    /** 按主键查询 */
    User selectById(@Param("id") Long id);

    /** 用户总数 */
    long count();

    /** 修改密码 */
    int updatePassword(@Param("id") Long id, @Param("password") String password);
}
