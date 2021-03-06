package com.atguigu.gmall.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author zxc
 * @email zxc_cll@163.com
 * @date 2021-08-28 19:19:37
 */
public interface AttrService extends IService<AttrEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrEntity> queryAttrByCategoryIdOrTypeAndSearchType(Long cid, Integer type, Integer searchType);
}

