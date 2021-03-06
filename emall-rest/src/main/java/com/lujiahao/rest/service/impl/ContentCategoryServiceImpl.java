package com.lujiahao.rest.service.impl;

import com.lujiahao.common.pojo.EasyUITreeNode;
import com.lujiahao.common.pojo.TaotaoResult;
import com.lujiahao.mapping.mapper.TbContentCategoryMapper;
import com.lujiahao.mapping.pojo.TbContentCategory;
import com.lujiahao.mapping.pojo.TbContentCategoryExample;
import com.lujiahao.rest.service.ContentCategoryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 内容分类服务
 *
 * @author lujiahao
 * @version V1.0
 * @email jiahao.lu@qtparking.com
 * @create 2016-09-11 16:14
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {
    @Autowired
    private TbContentCategoryMapper contentCategoryMapper;

    @Override
    public List<EasyUITreeNode> getContentCategoryList(long parentId) {
        TbContentCategoryExample example = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
        // 创建结果集合
        List<EasyUITreeNode> resultList = new ArrayList<>();
        for (TbContentCategory tbContentCategory : list) {
            EasyUITreeNode node = new EasyUITreeNode();
            node.setId(tbContentCategory.getId());
            node.setParentId(tbContentCategory.getParentId());
            node.setText(tbContentCategory.getName());
            node.setState(tbContentCategory.getIsParent() ? "closed" : "open");

            resultList.add(node);
        }
        return resultList;
    }

    @Override
    public TaotaoResult insertContentCategory(long parentId, String name) {
        TbContentCategory category = new TbContentCategory();
        category.setParentId(parentId);
        category.setName(name);
        category.setStatus(1);
        category.setSortOrder(1);
        category.setIsParent(false);
        category.setCreated(new Date());
        category.setUpdated(new Date());
        contentCategoryMapper.insert(category);

        TbContentCategory parentCat = contentCategoryMapper.selectByPrimaryKey(parentId);
        if (!parentCat.getIsParent()) {
            // 不是父节点,更改为父节点
            parentCat.setIsParent(true);
            contentCategoryMapper.updateByPrimaryKey(parentCat);
        }
        // 返回结果
        return TaotaoResult.ok(category);
    }

    @Override
    public TaotaoResult deleteContentCategory(long parentId, long id) {
        // 先根据id查找对应记录
        TbContentCategory tbContentCategory = contentCategoryMapper.selectByPrimaryKey(id);
        if (tbContentCategory.getIsParent()) {
            // 是父节点,则先删除子节点,然后再删除父节点
            TbContentCategoryExample example = new TbContentCategoryExample();
            TbContentCategoryExample.Criteria criteria = example.createCriteria();
            criteria.andParentIdEqualTo(id);
            List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
            for (TbContentCategory category : list) {
                contentCategoryMapper.deleteByPrimaryKey(category.getId());
            }
            // 子节点删除完毕后删除父节点
            contentCategoryMapper.deleteByPrimaryKey(id);
        } else {
            // 如果是子节点,直接删除
            contentCategoryMapper.deleteByPrimaryKey(id);
            // 判断parentId对应的记录下是否有子节点,如果没有子节点,就把isParent改成false
            TbContentCategoryExample example = new TbContentCategoryExample();
            TbContentCategoryExample.Criteria criteria = example.createCriteria();
            criteria.andIdEqualTo(parentId);
            List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
            TbContentCategory parentCat = list.get(0);
            if (list.size() > 0 && list.size() == 1) {
                // 没有子节点
                parentCat.setIsParent(false);
                contentCategoryMapper.updateByPrimaryKeySelective(parentCat);
            }
        }
        return TaotaoResult.ok();
    }

    @Override
    public TaotaoResult updateContentCategory(long id, String name) {
        TbContentCategory tbContentCategory = contentCategoryMapper.selectByPrimaryKey(id);
        if (tbContentCategory != null) {
            tbContentCategory.setName(name);
            contentCategoryMapper.updateByPrimaryKey(tbContentCategory);
            return TaotaoResult.ok();
        } else {
            return TaotaoResult.build(400,"更新失败");
        }
    }
}
