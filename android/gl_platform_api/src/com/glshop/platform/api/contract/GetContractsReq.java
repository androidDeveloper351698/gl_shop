package com.glshop.platform.api.contract;

import java.util.ArrayList;
import java.util.List;

import com.glshop.platform.api.DataConstants.BuyType;
import com.glshop.platform.api.DataConstants.ContractDraftStatus;
import com.glshop.platform.api.DataConstants.ContractLifeCycle;
import com.glshop.platform.api.DataConstants.ContractStatusType;
import com.glshop.platform.api.DataConstants.ContractType;
import com.glshop.platform.api.IReturnCallback;
import com.glshop.platform.api.base.BaseRequest;
import com.glshop.platform.api.contract.data.GetContractsResult;
import com.glshop.platform.api.contract.data.model.ContractSummaryInfoModel;
import com.glshop.platform.net.base.ResultItem;
import com.glshop.platform.utils.BeanUtils;

/**
 * @Description : 获取我的合同列表请求
 * @Copyright   : GL. All Rights Reserved
 * @Company     : 深圳市国立数码动画有限公司
 * @author      : 叶跃丰
 * @version     : 1.0
 * Create Date  : 2014-7-24 上午11:21:49
 */
public class GetContractsReq extends BaseRequest<GetContractsResult> {

	/**
	 * 合同类型
	 */
	public ContractType contractType = ContractType.UNCONFIRMED;

	/**
	 * 当前页数
	 */
	public int pageIndex;

	/**
	 * 每页条数
	 */
	public int pageSize;

	public GetContractsReq(Object invoker, IReturnCallback<GetContractsResult> callBackx) {
		super(invoker, callBackx);
	}

	@Override
	protected GetContractsResult getResultObj() {
		return new GetContractsResult();
	}

	@Override
	protected void buildParams() {
		request.addParam("status", contractType.toValue());
		request.addParam("pageIndex", pageIndex);
		request.addParam("pageSize", pageSize);
	}

	@Override
	protected void parseData(GetContractsResult result, ResultItem item) {
		List<ResultItem> items = (ArrayList<ResultItem>) item.get("DATA");
		List<ContractSummaryInfoModel> itemList = new ArrayList<ContractSummaryInfoModel>();
		if (BeanUtils.isNotEmpty(items)) {
			for (ResultItem contractItem : items) {
				ContractSummaryInfoModel info = new ContractSummaryInfoModel();
				info.contractType = ContractType.convert(contractItem.getEnumValue("status"));
				info.lifeCycle = ContractLifeCycle.convert(contractItem.getEnumValue("lifecycle"));
				info.statusType = ContractStatusType.convert(contractItem.getEnumValue("otype"));
				info.contractId = contractItem.getString("id");
				info.productName = contractItem.getString("productName");
				info.productCode = contractItem.getString("productCode");
				info.productSubCode = contractItem.getString("productType");
				info.productSpecId = contractItem.getString("productId");
				info.amount = contractItem.getString("totalnum");
				info.unitPrice = contractItem.getString("price");
				info.buyType = BuyType.convert(contractItem.getEnumValue("saleType"));
				info.buyCompanyId = contractItem.getString("buyerid");
				info.buyCompanyName = contractItem.getString("buyerName");
				info.sellCompanyId = contractItem.getString("sellerid");
				info.sellCompanyName = contractItem.getString("sellerName");
				info.createTime = contractItem.getString("creatime");
				info.expireTime = contractItem.getString("limittime");
				info.draftExpireTime = contractItem.getString("draftLimitTime");
				info.payExpireTime = contractItem.getString("payGoodsLimitTime");
				info.buyerDraftStatus = ContractDraftStatus.convert(contractItem.getEnumValue("buyerDraftStatus"));
				info.sellerDraftStatus = ContractDraftStatus.convert(contractItem.getEnumValue("sellerDraftStatus"));
				itemList.add(info);
			}
		}
		result.datas = itemList;
	}

	@Override
	protected String getTypeURL() {
		return "/contract/getMyContractListWithPaginiation";
	}

}
