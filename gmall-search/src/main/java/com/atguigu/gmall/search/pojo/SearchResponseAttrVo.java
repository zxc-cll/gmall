package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author zstars
 * @create 2021-09-13 17:56
 */
@Data
public class SearchResponseAttrVo {
    private Long attrId;
    private String attrName;
    private List<String> attrValues;
}
