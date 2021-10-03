package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author zstars
 * @create 2021-09-10 19:55
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
