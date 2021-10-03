package com.atguigu.gmall.search.demo;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SpuEntity;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author zstars
 * @create 2021-09-10 20:11
 */
@SpringBootTest
public class demo {
    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Test
   public void test01(){
        PageParamVo pageParamVo = new PageParamVo();
        pageParamVo.setPageNum(1);
        pageParamVo.setPageSize(10);
        ResponseVo<List<SpuEntity>> listResponseVo = this.gmallPmsClient.querySpuJson(pageParamVo);
        System.out.println(listResponseVo);
    }
}
