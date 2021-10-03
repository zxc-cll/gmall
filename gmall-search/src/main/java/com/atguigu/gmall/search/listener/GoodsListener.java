package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zstars
 * @create 2021-10-02 5:25
 */
@Component
public class GoodsListener {
    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GoodsRepository goodsRepository;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("SEARCH_INSERT_QUEUE"),
            exchange = @Exchange(value = "PMS_SPU_EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void syncData(Long spuId, Channel channel, Message message) throws IOException {
        if (spuId == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 同步数据
        // 根据spuId查询spu
        ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if (spuEntity == null){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 根据spuId查询spu下所有的sku
        ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkuBySpuId(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        // 如果sku的集合不为空，才需要把sku集合转化成goods集合
        if (!CollectionUtils.isEmpty(skuEntities)) {

            // 5.只有sku不为空时，才需要取查询品牌，如果sku为空了，不需要转化为goods，进而就不需要品牌
            // 因为同一个spu下，所有sku的品牌都是一样的
            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(spuEntity.getBrandId());
            BrandEntity brandEntity = brandEntityResponseVo.getData();

            // 6.查询分类
            ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(spuEntity.getCategoryId());
            CategoryEntity categoryEntity = categoryEntityResponseVo.getData();

            // 3.把sku集合转化成goods集合
            List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                Goods goods = new Goods();
                // sku相关参数
                goods.setSkuId(skuEntity.getId());
                goods.setDefaultImage(skuEntity.getDefaultImage());
                goods.setTitle(skuEntity.getTitle());
                goods.setSubtitle(skuEntity.getSubtitle());
                goods.setPrice(skuEntity.getPrice());

                // spu相关的参数
                goods.setCreateTime(spuEntity.getCreateTime());

                // 4.查询库存信息
                ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    goods.setSales(wareSkuEntities.stream().mapToLong(WareSkuEntity::getSales).reduce((a, b) -> a + b).getAsLong());
                    goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
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
                if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                    searchAttrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                        BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }

                // 8.基本类型的检索规格参数
                ResponseVo<List<SpuAttrValueEntity>> baseSearchAttrResponseVo = this.pmsClient.querySearchAttrValueByCidAndSpuId(spuEntity.getCategoryId(), spuEntity.getId());
                List<SpuAttrValueEntity> spuAttrValueEntities = baseSearchAttrResponseVo.getData();
                if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                    searchAttrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                        SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                        BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                        return searchAttrValueVo;
                    }).collect(Collectors.toList()));
                }

                goods.setSearchAttrs(searchAttrValueVos);

                return goods;
            }).collect(Collectors.toList());
            // 批量导入到索引库
            this.goodsRepository.saveAll(goodsList);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}
