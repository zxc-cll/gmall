package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.entity.PurchaseDetailEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 
 *
 * @author zxc
 * @email zxc_cll@163.com
 * @date 2021-08-28 19:39:23
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

