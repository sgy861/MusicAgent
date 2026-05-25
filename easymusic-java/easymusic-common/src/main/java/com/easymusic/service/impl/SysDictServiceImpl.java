package com.easymusic.service.impl;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.query.SysDictQuery;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.exception.BusinessException;
import com.easymusic.mappers.SysDictMapper;
import com.easymusic.redis.RedisComponent;
import com.easymusic.service.SysDictService;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 业务接口实现
 */
@Service("sysDictService")
public class SysDictServiceImpl implements SysDictService {

    @Resource
    private SysDictMapper<SysDict, SysDictQuery> sysDictMapper;

    @Resource
    private RedisComponent redisComponent;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<SysDict> findListByParam(SysDictQuery param) {
        return this.sysDictMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(SysDictQuery param) {
        return this.sysDictMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<SysDict> findListByPage(SysDictQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<SysDict> list = this.findListByParam(param);
        PaginationResultVO<SysDict> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(SysDict bean) {
        return this.sysDictMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<SysDict> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysDictMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<SysDict> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysDictMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(SysDict bean, SysDictQuery param) {
        StringTools.checkParam(param);
        return this.sysDictMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(SysDictQuery param) {
        StringTools.checkParam(param);
        return this.sysDictMapper.deleteByParam(param);
    }

    /**
     * 根据DictId获取对象
     */
    @Override
    public SysDict getSysDictByDictId(Integer dictId) {
        return this.sysDictMapper.selectByDictId(dictId);
    }

    /**
     * 根据DictId修改
     */
    @Override
    public Integer updateSysDictByDictId(SysDict bean, Integer dictId) {
        return this.sysDictMapper.updateByDictId(bean, dictId);
    }

    /**
     * 根据DictId删除
     */
    @Override
    public Integer deleteSysDictByDictId(Integer dictId) {
        return this.sysDictMapper.deleteByDictId(dictId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSysDict(SysDict sysDict) {
        if (sysDict.getDictId() == null) {
            sysDict.setSort(0);
            this.sysDictMapper.insert(sysDict);
        } else {
            SysDict dbInfo = this.sysDictMapper.selectByDictId(sysDict.getDictId());
            this.sysDictMapper.updateByDictId(sysDict, sysDict.getDictId());
            if (Constants.ZERO_STR.equals(sysDict.getDictPcode()) && !dbInfo.getDictCode().equals(sysDict.getDictCode())) {
                SysDict updateInfo = new SysDict();
                updateInfo.setDictPcode(sysDict.getDictCode());
                SysDictQuery dictQuery = new SysDictQuery();
                dictQuery.setDictPcode(dbInfo.getDictCode());
                this.sysDictMapper.updateByParam(updateInfo, dictQuery);
            }
        }
        SysDictQuery sysDictQuery = new SysDictQuery();
        sysDictQuery.setDictCode(sysDict.getDictCode());
        Integer count = sysDictMapper.selectCount(sysDictQuery);
        if (count > 1 && Constants.ZERO_STR.equals(sysDict.getDictPcode())) {
            throw new BusinessException("编号不能重复");
        }

        saveDict2Redis(sysDict.getDictPcode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delSysDictByDictId(Integer dictId) {
        SysDict sysDict = this.sysDictMapper.selectByDictId(dictId);
        this.sysDictMapper.deleteByDictId(dictId);
        SysDictQuery dictQuery = new SysDictQuery();
        dictQuery.setDictPcode(sysDict.getDictCode());
        this.sysDictMapper.deleteByParam(dictQuery);

        saveDict2Redis(sysDict.getDictPcode());
    }

    @Override
    public void changeSort(String dictPcode, String dictIds) {
        String[] dictIdArray = dictIds.split(",");
        List<SysDict> sysDictList = new ArrayList<>();
        for (int i = 0; i < dictIdArray.length; i++) {
            SysDict sysDict = new SysDict();
            sysDict.setSort(i + 1);
            sysDict.setDictId(Integer.parseInt(dictIdArray[i]));
            sysDictList.add(sysDict);
        }
        this.sysDictMapper.changeSort(sysDictList);

        saveDict2Redis(dictPcode);
    }

    private void saveDict2Redis(String dictPcode) {
        if (Constants.ZERO_STR.equals(dictPcode)) {
            return;
        }
        SysDictQuery dictQuery = new SysDictQuery();
        dictQuery.setDictPcode(dictPcode);
        dictQuery.setOrderBy("sort asc");
        List<SysDict> sysDictList = this.sysDictMapper.selectList(dictQuery);
        redisComponent.saveDict(dictPcode, sysDictList);
    }

    @Override
    public Map<String, List<SysDict>> getDictFromCache() {
        Map<String, List<SysDict>> allData = redisComponent.getAllDict();
        if (allData == null || allData.isEmpty()) {
            SysDictQuery dictQuery = new SysDictQuery();
            List<SysDict> sysDictList = this.sysDictMapper.selectList(dictQuery);
            Map<String, List<SysDict>> tempMap = sysDictList.stream().collect(Collectors.groupingBy(SysDict::getDictPcode));
            tempMap.forEach((k, v) -> {
                if (!Constants.ZERO_STR.equals(k)) {
                    redisComponent.saveDict(k, v.stream().sorted(Comparator.comparing(SysDict::getSort)).collect(Collectors.toList()));
                }
            });
            allData = redisComponent.getAllDict();
        }
        return allData;
    }
}