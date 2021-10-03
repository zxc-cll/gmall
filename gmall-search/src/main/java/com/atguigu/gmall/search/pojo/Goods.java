package com.atguigu.gmall.search.pojo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author zstars
 * @create 2021-09-10 11:07
 */
@Accessors(chain = true)
@Data
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class Goods{

    // 商品列表所需字段
    @Id
    private Long skuId;
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;
    @Field(type = FieldType.Double)
    private BigDecimal price;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    @Field(type = FieldType.Keyword, index = false)
    private String subtitle;

    // 排序所需字段
    @Field(type = FieldType.Integer)
    private Long sales = 0l; // 销量
    @Field(type = FieldType.Date)
    private Date createTime; // 新品
    // 过滤字段
    @Field(type = FieldType.Boolean)
    private Boolean store = false; //  是否有货

    // 聚合相关字段
    @Field(type = FieldType.Long)
    private Long brandId; // 品牌聚合
    @Field(type = FieldType.Keyword)
    private String brandName; // 品牌名称
    @Field(type = FieldType.Keyword)
    private String logo; // 品牌logo

    @Field(type = FieldType.Long)
    private Long categoryId; // 分类id
    @Field(type = FieldType.Keyword)
    private String categoryName; // 分类名称

    @Field(type = FieldType.Nested) // 嵌套类型
    private List<SearchAttrValueVo> searchAttrs; // 检索类型的规格参数
}
