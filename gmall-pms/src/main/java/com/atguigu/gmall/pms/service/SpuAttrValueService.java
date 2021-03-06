package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * spu属性值
 *
 * @author zxc
 * @email zxc_cll@163.com
 * @date 2021-08-28 19:19:37
 */
public interface SpuAttrValueService extends IService<SpuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    void saveSpuAttrValue(SpuVo spu, Long spuId);

    List<SpuAttrValueEntity> querySearchAttrValueByCidAndSpuId(Long cid, Long spuId);
}

