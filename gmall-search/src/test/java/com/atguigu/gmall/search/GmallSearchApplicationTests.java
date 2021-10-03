package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void contextLoads() {
        if (!restTemplate.indexExists(Goods.class)){
            this.restTemplate.createIndex(Goods.class);
            this.restTemplate.putMapping(Goods.class);
        }

        Integer pageNum = 1;
        Integer pageSize = 100;

        do {
            //分批查询spu
            ResponseVo<List<SpuEntity>> resSpuVo = this.pmsClient.querySpuJson(new PageParamVo(pageNum, pageSize, null));
            //当前页的数据
            List<SpuEntity> spuEntities = resSpuVo.getData();
            //如果当前页集合为空那么直接结束
            if (CollectionUtils.isEmpty(spuEntities)) {
                return;
            }

            //遍历当前页spu的集合。查询spu下的sku
            for (SpuEntity spuEntity : spuEntities) {
                ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkuBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();

                //if健壮性判断如果不为空才转换保存
                if (!CollectionUtils.isEmpty(skuEntities)){

                    // 只有sku不为空时，才需要取查询品牌，如果sku为空了，不需要转化为goods，进而就不需要品牌
                    // 因为同一个spu下，所有sku的品牌都是一样的
                    ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(spuEntity.getBrandId());
                    BrandEntity brandEntity = brandEntityResponseVo.getData();

                    // 查询分类
                    ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(spuEntity.getCategoryId());
                    CategoryEntity categoryEntity = categoryEntityResponseVo.getData();


                    //把sku集合转化为goods集合
                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        goods.setSkuId(skuEntity.getId())
                                //sku的相关参数
                                .setDefaultImage(skuEntity.getDefaultImage())
                                .setPrice(skuEntity.getPrice())
                                .setTitle(skuEntity.getTitle())
                                .setSubtitle(skuEntity.getSubtitle())
                                //spu的相关参数
                                .setCreateTime(spuEntity.getCreateTime());

                        //查询库存
                        ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntitys = wareSkuResponseVo.getData();
                        //if健壮性判断如果不为空才保存
                        if (!CollectionUtils.isEmpty(wareSkuEntitys)){
                            //查询销量
                            goods.setSales(wareSkuEntitys.stream().mapToLong(WareSkuEntity::getSales).reduce((a,b) -> a + b).getAsLong())
                                    //查询是否有货
                                    .setStore(wareSkuEntitys.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0));
                        }

                        // 品牌
                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }

                        // 分类
                        if (categoryEntity != null) {
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        List<SearchAttrValueVo> searchAttrValueVos = new ArrayList<>();
                        // 7.销售类型的检索规格参数
                        ResponseVo<List<SkuAttrValueEntity>> saleSearchAttrResponseVo = this.pmsClient.querySearchAttrValueByCidAndSkuId(skuEntity.getCategoryId(), skuEntity.getId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = saleSearchAttrResponseVo.getData();
                        // 需要把List<SkuAttrValueEntity>转化成List<SearchAttrValueVo>，最后放入searchAttrValueVos
                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                            searchAttrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList()));
                        }

                        // 8.基本类型的检索规格参数
                        ResponseVo<List<SpuAttrValueEntity>> baseSearchAttrResponseVo = this.pmsClient.querySearchAttrValueByCidAndSpuId(spuEntity.getCategoryId(), spuEntity.getId());
                        List<SpuAttrValueEntity> spuAttrValueEntities = baseSearchAttrResponseVo.getData();
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                            searchAttrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList()));
                        }

                        goods.setSearchAttrs(searchAttrValueVos);


                        return goods;
                    }).collect(Collectors.toList());
                    //批量导入到索引库
                    this.goodsRepository.saveAll(goodsList);
                }
            }

            pageNum++;
            pageSize=spuEntities.size();
        }while (pageSize==100);



    }

}
