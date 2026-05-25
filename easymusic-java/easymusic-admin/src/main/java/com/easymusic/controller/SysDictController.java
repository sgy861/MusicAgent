package com.easymusic.controller;

import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.SysDict;
import com.easymusic.entity.query.SysDictQuery;
import com.easymusic.entity.vo.ResponseVO;
import com.easymusic.service.SysDictService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller
 */
@RestController
@RequestMapping("/settings")
public class SysDictController extends ABaseController {

    @Resource
    private SysDictService sysDictService;

    /**
     * 根据条件分页查询
     */
    @RequestMapping("/loadSysDictList")
    public ResponseVO loadSysDictList(SysDictQuery query) {
        query.setOrderBy("sort asc");
        query.setPageSize(PageSize.SIZE50.getSize());
        return getSuccessResponseVO(sysDictService.findListByPage(query));
    }

    @RequestMapping("/saveSysDict")
    public ResponseVO saveSysDict(SysDict sysDict) {
        sysDictService.saveSysDict(sysDict);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delSysDict")
    public ResponseVO delSysDict(@NotNull Integer dictId) {
        sysDictService.delSysDictByDictId(dictId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeSort")
    public ResponseVO changeSort(@NotEmpty String dictPcode, @NotEmpty String dictIds) {
        sysDictService.changeSort(dictPcode, dictIds);
        return getSuccessResponseVO(null);
    }
}