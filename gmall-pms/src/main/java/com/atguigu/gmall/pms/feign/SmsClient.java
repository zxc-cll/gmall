package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author zstars
 * @create 2021-09-04 21:13
 */
@FeignClient("sms-service")
public interface SmsClient extends GmallSmsApi {


}
