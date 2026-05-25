package com.easymusic.service.impl;

import com.easymusic.entity.constants.Constants;
import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.po.ProductInfo;
import com.easymusic.entity.query.ProductInfoQuery;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.mappers.ProductInfoMapper;
import com.easymusic.service.ProductInfoService;
import com.easymusic.utils.FileUtils;
import com.easymusic.utils.StringTools;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 业务接口实现
 */
@Service("productInfoService")
public class ProductInfoServiceImpl implements ProductInfoService {

    @Resource
    private ProductInfoMapper<ProductInfo, ProductInfoQuery> productInfoMapper;

    @Resource
    private FileUtils fileUtils;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ProductInfo> findListByParam(ProductInfoQuery param) {
        return this.productInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ProductInfoQuery param) {
        return this.productInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ProductInfo> findListByPage(ProductInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ProductInfo> list = this.findListByParam(param);
        PaginationResultVO<ProductInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ProductInfo bean) {
        return this.productInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ProductInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.productInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ProductInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.productInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 多条件更新
     */
    @Override
    public Integer updateByParam(ProductInfo bean, ProductInfoQuery param) {
        StringTools.checkParam(param);
        return this.productInfoMapper.updateByParam(bean, param);
    }

    /**
     * 多条件删除
     */
    @Override
    public Integer deleteByParam(ProductInfoQuery param) {
        StringTools.checkParam(param);
        return this.productInfoMapper.deleteByParam(param);
    }

    /**
     * 根据ProductId获取对象
     */
    @Override
    public ProductInfo getProductInfoByProductId(String productId) {
        return this.productInfoMapper.selectByProductId(productId);
    }

    /**
     * 根据ProductId修改
     */
    @Override
    public Integer updateProductInfoByProductId(ProductInfo bean, String productId) {
        return this.productInfoMapper.updateByProductId(bean, productId);
    }

    /**
     * 根据ProductId删除
     */
    @Override
    public Integer deleteProductInfoByProductId(String productId) {
        return this.productInfoMapper.deleteByProductId(productId);
    }

    @Override
    public void saveProduct(MultipartFile coverFile, ProductInfo productInfo) {
        if (coverFile != null) {
            String cover = fileUtils.uploadFile(coverFile, null, null);
            productInfo.setCover(cover);
        }
        if (!StringTools.isEmpty(productInfo.getProductId())) {
            this.productInfoMapper.updateByProductId(productInfo, productInfo.getProductId());
        } else {
            productInfo.setSort(0);
            productInfo.setCreateTime(new Date());
            productInfo.setProductId(StringTools.getRandomNumber(Constants.LENGTH_5));
            this.productInfoMapper.insert(productInfo);
        }
    }

    @Override
    public void changeProductSort(String productIds) {
        String[] productIdArray = productIds.split(",");
        List<ProductInfo> productInfoList = new ArrayList<>();
        for (int i = 0; i < productIdArray.length; i++) {
            ProductInfo productInfo = new ProductInfo();
            productInfo.setSort(i + 1);
            productInfo.setProductId(productIdArray[i]);
            productInfoList.add(productInfo);
        }
        this.productInfoMapper.changeProductSort(productInfoList);
    }
}