package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @author zstars
 * @create 2021-09-13 17:53
 */
@Data
public class SearchResponseVo {
    //品牌列表
    private List<BrandEntity> brands;

    //分类列表
    private List<CategoryEntity> categories;

    // 规格参数列表
    private List<SearchResponseAttrVo> filters;

    // 分页参数
    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    // 当前页记录
    private List<Goods> goodsList;
}
