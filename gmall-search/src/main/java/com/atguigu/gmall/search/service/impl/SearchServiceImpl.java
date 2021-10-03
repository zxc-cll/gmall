package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zstars
 * @create 2021-09-13 15:37
 */
@Service("searchService")
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchParamVo paramVo) {

        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(paramVo));
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(response);

            // 结果集出所需的结果集
            SearchResponseVo responseVo = parseResult(response);
            // 分页结果集需要通过搜索参数获取
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());
            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 解析搜索结果集
     *
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();

        // 解析hits结果集
        SearchHits hits = response.getHits();
        // 总记录数
        responseVo.setTotal(hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        responseVo.setGoodsList(Stream.of(hitsHits).map(searchHit -> {
            // 获取_source，反序列化为goods数据模型
            String json = searchHit.getSourceAsString();
            Goods goods = JSON.parseObject(json, Goods.class);

            // 获取高亮结果集
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            goods.setTitle(highlightField.fragments()[0].string());
            return goods;
        }).collect(Collectors.toList()));

        // 解析aggregation结果集
        Aggregations aggregations = response.getAggregations();
        ParsedLongTerms brandIdAgg = aggregations.get("brandIdAgg");
        List<? extends Terms.Bucket> brandIdBuckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandIdBuckets)){
            // 把桶的集合转化成品牌集合
            responseVo.setBrands(brandIdBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                // 每个桶中的key就是品牌的id
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                // 获取所有的子聚合
                Aggregations subAggs = ((Terms.Bucket) bucket).getAggregations();

                // 获取了品牌名称的子聚合
                ParsedStringTerms brandNameAgg = subAggs.get("brandNameAgg");
                // 获取品牌名称子聚合中的桶
                List<? extends Terms.Bucket> nameBuckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameBuckets)){
                    // 品牌名称这个子聚合应该有且仅有一个元素
                    brandEntity.setName(nameBuckets.get(0).getKeyAsString());
                }

                // 获取logo子聚合
                ParsedStringTerms logoAgg = subAggs.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)){
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList()));
        }

        // 分类
        ParsedLongTerms categoryIdAgg = aggregations.get("categoryIdAgg");
        List<? extends Terms.Bucket> buckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            responseVo.setCategories(buckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                ParsedStringTerms categoryNameAgg = ((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> nameBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameBuckets)){
                    categoryEntity.setName(nameBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList()));
        }

        // 规格参数的聚合结果集
        ParsedNested attrAgg = aggregations.get("attrAgg");
        // 获取嵌套聚合结果集中规格参数id子聚合
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrBuckets)){
            // 把桶集合转化成SearchResponseAttrVo集合
            responseVo.setFilters(attrBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                // 当前桶中的key就是规格参数id
                searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());

                // 获取当前桶中的所有子聚合
                Aggregations subAggs = ((Terms.Bucket) bucket).getAggregations();

                // 获取规格参数名称的子聚合
                ParsedStringTerms attrNameAgg = subAggs.get("attrNameAgg");
                List<? extends Terms.Bucket> nameBuckets = attrNameAgg.getBuckets();
                // 获取规格参数名称子聚合中的桶，有且仅有一个元素
                if (!CollectionUtils.isEmpty(nameBuckets)){
                    searchResponseAttrVo.setAttrName(nameBuckets.get(0).getKeyAsString());
                }

                // 获取规格参数值的子聚合
                ParsedStringTerms attrValueAgg = subAggs.get("attrValueAgg");
                List<? extends Terms.Bucket> valueBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(valueBuckets)){
                    searchResponseAttrVo.setAttrValues(valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList()));
        }

        return responseVo;
    }



    /**
     * 构建搜索条件
     *
     */
    private SearchSourceBuilder buildDsl(SearchParamVo searchParamVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //获取搜索的关键字
        String keyword = searchParamVo.getKeyword();

        if (StringUtils.isBlank(keyword)) {
            throw new RuntimeException("搜索关键字不能为空");
        }
        //1、构建查询过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder);
        //1.1、构建匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));

        // 1.2. 构建过滤条件
        // 1.2.1. 构建品牌过滤
        List<Long> brandId = searchParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        // 1.2.2. 构建分类过滤
        List<Long> categoryId = searchParamVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        // 1.2.3. 构建价格区间
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");

            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }

            boolQueryBuilder.filter(rangeQuery);
        }
        // 1.2.4. 构建是否有货
        Boolean store = searchParamVo.getStore();
        if (store != null) { // 仅显示有货的可以加上&&store，我们做有货无货都查询
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));

        }
        // 1.2.5. 构建规格参数过滤
        List<String> props = searchParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> { //  4:8G-12G
                String[] attr = StringUtils.split(prop, ":"); // 用:分割
                if (attr != null && attr.length == 2) { // 当分割后的值不为空，并长度为2，才是合法的
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                    // 数组的第一个为是规格参数id
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));

                    // 规格参数值：8G-12G，再以-分割
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                }
            });
        }

        // 2.构建排序条件 0-得分排序 1-价格降序 2-价格升序 3-销量降序 4-新品降序
        Integer sort = searchParamVo.getSort();
        if (sort != null) {
            switch (sort) {
                case 1:
                    sourceBuilder.sort("price", SortOrder.DESC);
                    break;
                case 2:
                    sourceBuilder.sort("price", SortOrder.ASC);
                    break;
                case 3:
                    sourceBuilder.sort("sales", SortOrder.DESC);
                    break;
                case 4:
                    sourceBuilder.sort("createTime", SortOrder.DESC);
                    break;
                default:
                    sourceBuilder.sort("_score", SortOrder.DESC);
                    break;
            }
        }
        // 3.构建分页条件
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        // 4.构建高亮
        sourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font style='color:red;'>")
                .postTags("</font>"));

        // 5.构建聚合条件
        // 5.1. 构建品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        // 5.2. 构建分类聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        // 5.3. 构建规格参数聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));

        // 6.构建结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "subtitle", "price", "defaultImage"}, null);

        System.out.println(sourceBuilder);

        return sourceBuilder;

    }
}



















