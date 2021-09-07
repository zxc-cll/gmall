package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author zstars
 * @create 2021-09-07 9:00
 */
public interface GmallSmsApi {
    @PostMapping("sms/skubounds/saveSkuSale")
    public ResponseVo saveSkuSale(@RequestBody SkuSaleVo skuSaleVo);
}
