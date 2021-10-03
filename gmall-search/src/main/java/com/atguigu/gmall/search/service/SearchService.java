package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;

/**
 * @author zstars
 * @create 2021-09-13 15:36
 */
public interface SearchService {

    SearchResponseVo search(SearchParamVo searchParamVo);
}
