package com.easymusic.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Service;

import com.easymusic.entity.enums.PageSize;
import com.easymusic.entity.query.PayCodeInfoQuery;
import com.easymusic.entity.po.PayCodeInfo;
import com.easymusic.entity.vo.PaginationResultVO;
import com.easymusic.entity.query.SimplePage;
import com.easymusic.mappers.PayCodeInfoMapper;
import com.easymusic.service.PayCodeInfoService;
import com.easymusic.utils.StringTools;


/**
 * 支付码 业务接口实现
 */
@Service("payCodeInfoService")
public class PayCodeInfoServiceImpl implements PayCodeInfoService {

	@Resource
	private PayCodeInfoMapper<PayCodeInfo, PayCodeInfoQuery> payCodeInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<PayCodeInfo> findListByParam(PayCodeInfoQuery param) {
		return this.payCodeInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(PayCodeInfoQuery param) {
		return this.payCodeInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<PayCodeInfo> findListByPage(PayCodeInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<PayCodeInfo> list = this.findListByParam(param);
		PaginationResultVO<PayCodeInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(PayCodeInfo bean) {
		return this.payCodeInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<PayCodeInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.payCodeInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<PayCodeInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.payCodeInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(PayCodeInfo bean, PayCodeInfoQuery param) {
		StringTools.checkParam(param);
		return this.payCodeInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(PayCodeInfoQuery param) {
		StringTools.checkParam(param);
		return this.payCodeInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据PayCode获取对象
	 */
	@Override
	public PayCodeInfo getPayCodeInfoByPayCode(String payCode) {
		return this.payCodeInfoMapper.selectByPayCode(payCode);
	}

	/**
	 * 根据PayCode修改
	 */
	@Override
	public Integer updatePayCodeInfoByPayCode(PayCodeInfo bean, String payCode) {
		return this.payCodeInfoMapper.updateByPayCode(bean, payCode);
	}

	/**
	 * 根据PayCode删除
	 */
	@Override
	public Integer deletePayCodeInfoByPayCode(String payCode) {
		return this.payCodeInfoMapper.deleteByPayCode(payCode);
	}
}