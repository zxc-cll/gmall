package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author zxc
 * @email zxc_cll@163.com
 * @date 2021-08-28 19:38:45
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
