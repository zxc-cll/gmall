package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    SpuMapper spuMapper;
    @Autowired
    SpuDescService spuDescService;
    @Autowired
    SpuAttrValueService spuAttrValueService;
    @Autowired
    SkuService skuService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo getSpuByCidAndSearch(Long cid, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果分类的id不等于0，需要查本类
        if (cid != 0) {
            wrapper.eq("category_id", cid);
        }
        String key = pageParamVo.getKey();
        //判断非空
        if (StringUtils.isNotBlank(key)) {
            //默认wrapper条件之间都是and的关系，如果需要 or 可以再条件之间加上 .or()方法
            //但是这样的话不会先执行判断 or 的两个条件 需要加上小括号就要用上
            //wrapper.and()  .and()里面需要一个Consumer消费型函数式接口
            //t就代表了调用的对象本身，将条件用接口里面的t进行调用
            //如果 or()后面还需要小括号() ,那么就在or（）里面还可以用这个消费型函数式接口
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }

    //@Transactional(rollbackFor = Exception.class,timeout = 3)
    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spu) {
        // 1.先保存spu相关信息
        // 1.1. 保存spu pms_spu
        this.save(spu);
        Long spuId = spu.getId();
        // 1.2. 保存pms_spu_desc 不会回滚 --> spu 19 --> spuDesc 20(如果依然是19，说明回滚了)
        this.spuDescService.saveSpuDesc(spu,spuId);
//        int i =1/0;

        // 1.3. 保存基本属性值 pms_spu_attr_value
        this.spuAttrValueService.saveSpuAttrValue(spu,spuId);
        // 2.保存sku相关信息
        // 2.1. 保存sku pms_sku
        this.skuService.saveSku(spu,spuId);
        // 2.2.
        // 2.3. 保存销售属性pms_sku_attr_value

        // 3.保存营销信息（skuId）
 //       int i =1/0;

        this.rabbitTemplate.convertAndSend("PMS_SPU_EXCHANGE","item.insert",spuId);


    }

}