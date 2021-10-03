package com.atguigu.gmall.search.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author zstars
 * @create 2021-09-13 15:32
 */
@Data
@Accessors(chain = true)
public class SearchParamVo {

    // 搜索关键字
    private String keyword;

    // 品牌的过滤条件
    private List<Long> brandId;

    // 分类的过滤条件
    private List<Long> categoryId;

    // 规格参数的过滤条件["4:8G-12G", "5:128G-256G-512G"]
    private List<String> props;

    // 排序条件：0-得分排序 1-价格降序 2-价格升序 3-销量降序 4-新品降序
    private Integer sort;

    // 价格范围过滤条件
    private Double priceFrom;
    private Double priceTo;

    // 分页参数
    private Integer pageNum = 1;
    private final Integer pageSize = 20;

    // 是否有货
    private Boolean store;
}
