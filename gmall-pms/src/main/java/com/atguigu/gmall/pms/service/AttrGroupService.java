package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.GroupVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zxc
 * @email zxc_cll@163.com
 * @date 2021-08-28 19:19:37
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<AttrGroupEntity> getAttrGroupList(Long cid);

    List<GroupVo> queryAttrAndAttrGroupByCategoryId(Long cid);
}

