package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> getAttrGroupList(Long cid) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        if (cid!=null){
            wrapper.eq("category_id",cid);
        }
        List<AttrGroupEntity> list = this.list(wrapper);
        return list;
    }

    @Override
    public List<GroupVo> queryAttrAndAttrGroupByCategoryId(Long cid) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id",cid);
        List<AttrGroupEntity> list = this.list(wrapper);
        if (CollectionUtils.isEmpty(list)) return null;

        //stream方法
        List<GroupVo> collect = list.stream().map(attrGroupEntity -> {
            GroupVo groupVo = new GroupVo();
            BeanUtils.copyProperties(attrGroupEntity,groupVo);

            List<AttrEntity> attrEntityList = attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id",attrGroupEntity.getId())
                    .eq("type", 1));
            groupVo.setAttrEntities(attrEntityList);
            return groupVo;
        }).collect(Collectors.toList());

//        ArrayList<GroupVo> groupVoArrayList = new ArrayList<>();
//        for (AttrGroupEntity attrGroup : list) {
//            GroupVo groupVo = new GroupVo();
//            BeanUtils.copyProperties(attrGroup,groupVo);
//            List<AttrEntity> attrEntityList = attrMapper.selectList(new QueryWrapper<AttrEntity>()
//                                                                    .eq("category_id", cid)
//                                                                    .eq("group_id",groupVo.getId())
//                                                                    .eq("type", 1));
//
//            groupVo.setAttrEntities(attrEntityList);
//            groupVoArrayList.add(groupVo);
//        }

        return collect;
    }


}