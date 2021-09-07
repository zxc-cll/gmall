package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @author zstars
 * @create 2021-09-04 18:56
 */
@Data
public class SpuVo extends SpuEntity {

    private List<String> spuImages;
    private List<SpuAttrValueVo> baseAttrs;
    private List<SkuVo> skus;

}
