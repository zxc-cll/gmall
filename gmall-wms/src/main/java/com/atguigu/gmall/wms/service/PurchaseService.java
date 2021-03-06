package com.atguigu.gmall.wms.service;

import com.atguigu.gmall.wms.entity.PurchaseEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 采购信息
 *
 * @author zxc
 * @email zxc_cll@163.com
 * @date 2021-08-28 19:39:23
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

