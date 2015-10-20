package com.glshop.net.ui.findbuy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.glshop.net.R;
import com.glshop.net.common.GlobalAction;
import com.glshop.net.common.GlobalConfig;
import com.glshop.net.common.GlobalConstants;
import com.glshop.net.common.GlobalConstants.DataStatus;
import com.glshop.net.common.GlobalConstants.TabStatus;
import com.glshop.net.common.GlobalMessageType;
import com.glshop.net.common.GlobalMessageType.MsgCenterMessageType;
import com.glshop.net.common.GlobalMessageType.UserMessageType;
import com.glshop.net.logic.advertising.IAdvertisingLogic;
import com.glshop.net.logic.buy.IBuyLogic;
import com.glshop.net.logic.cache.DataCenter;
import com.glshop.net.logic.cache.DataCenter.DataType;
import com.glshop.net.logic.message.IMessageLogic;
import com.glshop.net.logic.model.MenuItemInfo;
import com.glshop.net.logic.model.RespInfo;
import com.glshop.net.logic.syscfg.ISysCfgLogic;
import com.glshop.net.ui.MainActivity;
import com.glshop.net.ui.basic.BasicFragmentActivity;
import com.glshop.net.ui.basic.adapter.BannerAdapter;
import com.glshop.net.ui.basic.view.CustomPageIndicator;
import com.glshop.net.ui.basic.view.TodayPriceListView;
import com.glshop.net.ui.basic.view.cycleview.CycleViewPager;
import com.glshop.net.ui.basic.view.dialog.AlertxDialog;
import com.glshop.net.ui.basic.view.dialog.BaseDialog.IDialogCallback;
import com.glshop.net.ui.basic.view.dialog.menu.BaseMenuDialog.IMenuCallback;
import com.glshop.net.ui.basic.view.dialog.menu.MenuDialog;
import com.glshop.net.ui.myprofile.ProfileAuthActivity;
import com.glshop.net.ui.setting.MessageListActivity;
import com.glshop.net.ui.setting.SettingActivity;
import com.glshop.net.ui.user.LoginActivity;
import com.glshop.net.utils.MenuUtil;
import com.glshop.platform.api.DataConstants;
import com.glshop.platform.api.DataConstants.ProductType;
import com.glshop.platform.api.DataConstants.SysCfgCode;
import com.glshop.platform.api.advertising.data.model.BannerModel;
import com.glshop.platform.api.profile.data.model.ImageInfoModel;
import com.glshop.platform.api.syscfg.data.model.AreaInfoModel;
import com.glshop.platform.base.manager.LogicFactory;
import com.glshop.platform.utils.BeanUtils;
import com.glshop.platform.utils.Logger;
import com.glshop.platform.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description : 电商平台主页
 * @Copyright   : GL. All Rights Reserved
 * @Company     : 深圳市国立数码动画有限公司
 * @author      : 叶跃丰
 * @version     : 1.0
 * Create Date  : 2014-7-17 下午5:01:00
 */
public class ShopActivity extends BasicFragmentActivity implements ViewPager.OnPageChangeListener, TodayPriceListView.Callback {

	private static final String TAG = "ShopActivity";

	private static final String TAB_SAND = "tab-sand";
	private static final String TAB_STONE = "tab-stone";

	private ProductType productType = ProductType.SAND;
	private TodayPriceListView mLvSandTodayPrice;
	private TodayPriceListView mLvStoneTodayPrice;

	private RadioButton mRdoBtnSand;
	private RadioButton mRdoBtnStone;

	private CycleViewPager mViewPager;
	private BannerAdapter mPagerAdapter;
	private CustomPageIndicator mPageIndicator;

	private MenuDialog menuPortList;
	private TextView mTvItemPort;

	private ImageView iv_message;
	private View mBtnMessage;
	private View mBtnLogin;
	private View mBtnSetting;
	private View mIvPortMenuIcon;

	private String mAreaName = "";
	private Map<String, String> mAreaMap = new HashMap<String, String>();

	private IBuyLogic mBuyLogic;
	private IMessageLogic mMessageLogic;
	private ISysCfgLogic mSysCfgLogic;

	private IAdvertisingLogic iAdvertisingLogic;
	List<View> advertList=new ArrayList<View>();
	List<BannerModel> adverlistModel=new ArrayList<BannerModel>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.e(TAG, "onCreate()");
		setContentView(R.layout.activity_shop);
		initView();
		initData(savedInstanceState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Logger.e(TAG, "onNewIntent");
		refreshMessageStatus();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logger.i(TAG, "onResume");
		mViewPager.startAnimation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		Logger.i(TAG, "onPause");
		mViewPager.stopAnimation();
	}

	private void initView() {
		mBtnMessage = getView(R.id.btn_message_center);
		iv_message=getView(R.id.iv_msg);
		mBtnLogin = getView(R.id.btn_user_login);
		mBtnSetting = getView(R.id.btn_setting);
		mTvItemPort = getView(R.id.tv_port_item);
		mIvPortMenuIcon = getView(R.id.iv_port_dropdown);

		mLvSandTodayPrice = getView(R.id.ll_sand_today_price_list);
		mLvStoneTodayPrice = getView(R.id.ll_stone_today_price_list);
		mRdoBtnSand = getView(R.id.rdoBtn_product_sand);
		mRdoBtnStone = getView(R.id.rdoBtn_product_stone);
		mViewPager = getView(R.id.advert_viewpager);
		mViewPager.setOnPageChangeListener(this);
		mPageIndicator = (CustomPageIndicator) findViewById(R.id.advert_page_indicator);
		mLvSandTodayPrice.setCallback(this);
		mLvStoneTodayPrice.setCallback(this);

		getView(R.id.btn_user_login).setOnClickListener(this);
		getView(R.id.btn_message_center).setOnClickListener(this);
		getView(R.id.btn_price_forecast).setOnClickListener(this);
		getView(R.id.btn_find_buys).setOnClickListener(this);
		getView(R.id.btn_my_buys).setOnClickListener(this);
		getView(R.id.btn_my_contract).setOnClickListener(this);
		getView(R.id.btn_my_purse).setOnClickListener(this);
		getView(R.id.btn_my_profile).setOnClickListener(this);
		getView(R.id.btn_setting).setOnClickListener(this);
		getView(R.id.rdoBtn_product_sand).setOnClickListener(this);
		getView(R.id.rdoBtn_product_stone).setOnClickListener(this);
		getView(R.id.ll_port_list).setOnClickListener(this);
	}

	private void initData(Bundle savedState) {
		List<AreaInfoModel> areaList = mSysCfgLogic.getLocalPortList();
		if (BeanUtils.isNotEmpty(areaList)) {
			for (int i = 0; i < areaList.size(); i++) {
				AreaInfoModel info = areaList.get(i);
				mAreaMap.put(info.name, info.code);
			}
			mAreaName = areaList.get(0).name;
			mTvItemPort.setText(mAreaName);
		}

		if (savedState != null) {
			productType = ProductType.convert(savedState.getInt(GlobalAction.BuyAction.EXTRA_KEY_SELECTED_TAB));
			updateTabState();
			Logger.e(TAG, "TabIndex = " + productType.toValue());
		}

		switchView();

		getBannerAction();

		updateLoginStatus();

		if (GlobalConfig.getInstance().isTokenUpdated()) {
			refreshMessageStatus();
		}
	}

	/**
	 * 更新登录状态
	 */
	public void updateLoginStatus() {
		boolean isLogined = isLogined();
		mBtnLogin.setVisibility(isLogined ? View.GONE : View.VISIBLE);
		mBtnMessage.setVisibility(isLogined ? View.VISIBLE : View.GONE);
		mBtnSetting.setVisibility(isLogined ? View.VISIBLE : View.GONE);
	}

	/**
	 * 检测用户登录状态，如果未登录，则根据needLogin决定是否需要登录
	 * @param needLogin
	 * @return
	 */
	private boolean checkLoginStatus(boolean needLogin) {
		if (isLogined()) {
			return true;
		} else {
			if (needLogin) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
			}
			showToast(R.string.user_not_login);
			return false;
		}
	}

	private void switchView() {
		mLvSandTodayPrice.setVisibility(productType == ProductType.SAND ? View.VISIBLE : View.GONE);
		mLvStoneTodayPrice.setVisibility(productType == ProductType.STONE ? View.VISIBLE : View.GONE);
		String areaCode = mAreaMap.get(mAreaName);
		if (areaCode == null) {
			areaCode = "";
		}
		if (productType == ProductType.SAND && (!mLvSandTodayPrice.isLoaded() || !areaCode.equals(mLvSandTodayPrice.getAreaCode()))) {
			mLvSandTodayPrice.setAreaCode(areaCode);
			mBuyLogic.getTodayPrice(productType, SysCfgCode.TYPE_PRODUCT_SAND, areaCode);
		} else if (productType == ProductType.STONE && (!mLvStoneTodayPrice.isLoaded() || !areaCode.equals(mLvStoneTodayPrice.getAreaCode()))) {
			mLvStoneTodayPrice.setAreaCode(areaCode);
			mBuyLogic.getTodayPrice(productType, SysCfgCode.TYPE_PRODUCT_STONE, areaCode);
		}
	}

	private void updateTabState() {
		mRdoBtnSand.setChecked(productType == ProductType.SAND);
		mRdoBtnStone.setChecked(productType == ProductType.STONE);
	}

	/**
	 * 刷新未读消息个数
	 */
	private void refreshMessageStatus() {
		if (isLogined() && StringUtils.isNotEmpty(getCompanyId())) {
			mMessageLogic.getUnreadedNumberFromServer(getCompanyId());
		}
	}


	/**
	 * 获取广告图
	 */
	private void getBannerAction(){
		iAdvertisingLogic.getIndexBanner(GlobalConstants.CLIENT_TYPE);
	}

	@Override
	protected void handleStateMessage(Message message) {
		super.handleStateMessage(message);
		Logger.d(TAG, "handleStateMessage: what = " + message.what + " & RespInfo = " + message.obj);

		if (message.what == UserMessageType.MSG_LOGIN_SUCCESS || message.what == UserMessageType.MSG_REFRESH_TOKEN_SUCCESS) {
			if(GlobalConfig.getInstance().isAuthRemind()){
				AlertxDialog alert = new AlertxDialog(this, R.style.dialog);
				//mUpgradeTipsDialog.setContent(getString(R.string.upgrade_pkg_notify_title));
				alert.setCallback(new IDialogCallback() {
	
					@Override
					public void onConfirm(int type, Object obj) {
						Intent intent = new Intent(ShopActivity.this, ProfileAuthActivity.class);
						startActivity(intent);
					}
	
					@Override
					public void onCancel(int type) {
					}
				});
				alert.show();
			}
			refreshMessageStatus();
		}

		RespInfo respInfo = getRespInfo(message);
		switch (message.what) {
		case GlobalMessageType.BuyMessageType.MSG_GET_TODAY_PRICE_SUCCESS:
			onGetTodayPriceSuccess(respInfo);
			break;
		case GlobalMessageType.BuyMessageType.MSG_GET_TODAY_PRICE_FAILED:
			onGetTodayPriceFailed(respInfo);
			break;
		case MsgCenterMessageType.MSG_GET_SERVER_UNREAD_NUM_SUCCESS:
			onGetMessageNumSuccess(respInfo);
			break;
		case MsgCenterMessageType.MSG_GET_SERVER_UNREAD_NUM_FAILED:
			// TODO
			break;
		case MsgCenterMessageType.MSG_REFRESH_UNREAD_MSG_NUM:
			refreshMessageStatus();
			break;
		case GlobalMessageType.Advertising.BANNER_SUCCESS:
			onGetBannerSuccess(message);
			break;
			case GlobalMessageType.Advertising.BANNER_FAILED:
				onGetBannerFailed();
				break;
		case UserMessageType.MSG_LOGIN_SUCCESS: // 登录成功
		case UserMessageType.MSG_LOGIN_FAILED: // 登录失败
		case UserMessageType.MSG_LOGOUT_SUCCESS: // 注销成功
		case UserMessageType.MSG_LOGOUT_FAILED: // 注销失败
		case UserMessageType.MSG_MODIFY_PASSWORD_SUCCESS: // 密码修改成功
		case DataConstants.GlobalMessageType.MSG_USER_OFFLINE: // 用户离线
		case DataConstants.GlobalMessageType.MSG_USER_NOT_LOGINED: // 用户未登录
		case DataConstants.GlobalMessageType.MSG_USER_TOKEN_EXPIRE: // 用户Token失效
			updateLoginStatus();
			break;
		}
	}

	@SuppressWarnings("unchecked")
	private void onGetTodayPriceSuccess(RespInfo respInfo) {
		if (respInfo != null) {
			ProductType type = ProductType.convert(respInfo.intArg1);
			List data = DataCenter.getInstance().getData(getDataType(type));
			if (BeanUtils.isNotEmpty(data)) {
				if (type == ProductType.SAND) {
					mLvSandTodayPrice.setIsLoaded(true);
					mLvSandTodayPrice.setPriceList(data);
					mLvSandTodayPrice.updateDataStatus(DataStatus.NORMAL);
				} else {
					mLvStoneTodayPrice.setIsLoaded(true);
					mLvStoneTodayPrice.setPriceList(data);
					mLvStoneTodayPrice.updateDataStatus(DataStatus.NORMAL);
				}
			} else {
				if (type == ProductType.SAND) {
					mLvSandTodayPrice.updateDataStatus(DataStatus.EMPTY);
				} else {
					mLvStoneTodayPrice.updateDataStatus(DataStatus.EMPTY);
				}
			}
		}
	}

	private void onGetTodayPriceFailed(RespInfo respInfo) {
		if (respInfo != null) {
			if (ProductType.convert(respInfo.intArg1) == ProductType.SAND) {
				mLvSandTodayPrice.updateDataStatus(DataStatus.ERROR);
			} else {
				mLvStoneTodayPrice.updateDataStatus(DataStatus.ERROR);
			}
		}
	}

	private void onGetMessageNumSuccess(RespInfo respInfo) {
		if (respInfo != null) {
			int num = Integer.parseInt(String.valueOf(respInfo.data));
			if (mBtnMessage.getVisibility() == View.VISIBLE) {
				if (num > 0) {
					iv_message.setImageResource(R.mipmap.icon_message_notnull);
				} else {
					iv_message.setImageResource(R.mipmap.icon_message_null);
				}
			}
		}
	}

	/**
	 * 获取广告图成功
	 * @param message
	 */
	private void onGetBannerSuccess(Message message){
		//用于判断是否需要复制数据
		boolean flag=false;
		//用于显示指示器的个数
		int PageCount=0;
		if (null!=message){
			List<BannerModel> list= (List<BannerModel>) message.obj;
			if (BeanUtils.isNotEmpty(list)){
				advertList.clear();
				adverlistModel.clear();

				for (BannerModel bannerModel : list) {
				View view = View.inflate(this, R.layout.adapter_advert_item, null);
				    advertList.add(view);
					adverlistModel.add(bannerModel);

				}
				PageCount=advertList.size();
				//处理size小于4的情况
				if (advertList.size()<4&&advertList.size()>1){
					flag=true;
					for (BannerModel bannerModel : list) {
						View view = View.inflate(this, R.layout.adapter_advert_item, null);
						advertList.add(view);
						adverlistModel.add(bannerModel);
					}
				}
				if (advertList.size()==1){
					flag=true;
					String banner_url=adverlistModel.get(0).imageInfoModel.cloudUrl;
					String banner_linkeurl=adverlistModel.get(0).banner_linkurl;
					for (int i = 0; i < 4; i++) {
						BannerModel bannerModel=new BannerModel();
						bannerModel.banner_linkurl=banner_linkeurl;
						ImageInfoModel imageinfo=new ImageInfoModel();
						imageinfo.cloudUrl=banner_url;
						bannerModel.imageInfoModel=imageinfo;
						View view = View.inflate(this, R.layout.adapter_advert_item, null);
						advertList.add(view);
						adverlistModel.add(bannerModel);
					}
				}
				refreshBanner(flag,PageCount);
			}else {
				//如果为空则显示默认的四张
				onGetBannerFailed();
			}
		}

	}

	/**
	 * 加载广告图失败
	 */
	private void onGetBannerFailed(){
		View view = View.inflate(this, R.layout.adapter_advert_item, null);
		view.findViewById(R.id.iv_advert_bg).setBackgroundResource(R.drawable.bg_banner_advert_1);
		advertList.add(view);
		BannerModel bannermodel1=new BannerModel();
		bannermodel1.banner_linkurl="NO";//设置不可点击
		adverlistModel.add(bannermodel1);


		view = View.inflate(this, R.layout.adapter_advert_item, null);
		view.findViewById(R.id.iv_advert_bg).setBackgroundResource(R.drawable.bg_banner_advert_2);
		advertList.add(view);
		BannerModel bannermodel2=new BannerModel();
		bannermodel2.banner_linkurl="NO";//设置不可点击
		adverlistModel.add(bannermodel2);

		view = View.inflate(this, R.layout.adapter_advert_item, null);
		view.findViewById(R.id.iv_advert_bg).setBackgroundResource(R.drawable.bg_banner_advert_3);
		advertList.add(view);
		BannerModel bannermodel3=new BannerModel();
		bannermodel3.banner_linkurl="NO";//设置不可点击
		adverlistModel.add(bannermodel3);

		view = View.inflate(this, R.layout.adapter_advert_item, null);
		view.findViewById(R.id.iv_advert_bg).setBackgroundResource(R.drawable.bg_banner_advert_4);
		advertList.add(view);
		BannerModel bannermodel4=new BannerModel();
		bannermodel4.banner_linkurl="NO";//设置不可点击
		adverlistModel.add(bannermodel4);

		refreshBanner(false,4);
	}
	/**
	 * 刷新广告图
	 * @param flag 用来判断广告图是否小于4,处理pageadapter缓存为3的问题
	 * @param PageCount 提示器的个数
	 *
	 */
	private void refreshBanner(boolean flag,int PageCount){
		if (null==mPagerAdapter) {
			mPagerAdapter = new BannerAdapter(advertList, adverlistModel, this, true);

			mViewPager.setAdapter(mPagerAdapter);
		}else {
			mPagerAdapter.notifyDataSetChanged();
		}
		mViewPager.setOffscreenPageLimit(1);
		if (flag)
			mPageIndicator.setPageCount(PageCount);
		else
			mPageIndicator.setPageCount(advertList.size());
		mPageIndicator.setPageSelectedIndex(0);
		mViewPager.setCycleMoveEnabled(true);
	}

	private int getDataType(ProductType type) {
		int dataType = DataType.SAND_TODAY_PRICE_LIST;
		switch (type) {
		case SAND:
			dataType = DataType.SAND_TODAY_PRICE_LIST;
			break;
		case STONE:
			dataType = DataType.STONE_TODAY_PRICE_LIST;
			break;
		}
		return dataType;
	}

	private void showPortListMenu() {
		closeDialog(menuPortList);
		Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate180);
		rotateAnimation.setFillAfter(true);
		final Animation reverseAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_reverse180);
		reverseAnimation.setFillAfter(true);
		reverseAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				refreshData();
			}
		});

		mIvPortMenuIcon.startAnimation(rotateAnimation);

		//List<String> menu = Arrays.asList(getResources().getStringArray(R.array.port_type));
		List<MenuItemInfo> menu = MenuUtil.makeMenuAList(mSysCfgLogic.getLocalPortList());
		menuPortList = new MenuDialog(this, menu, new IMenuCallback() {

			@Override
			public void onConfirm(int type, Object obj) {
				mIvPortMenuIcon.startAnimation(reverseAnimation);
			}

			@Override
			public void onCancel(int type) {
				mIvPortMenuIcon.startAnimation(reverseAnimation);
			}

			@Override
			public void onMenuClick(int type, int position, Object obj) {
				mIvPortMenuIcon.startAnimation(reverseAnimation);
				mAreaName = ((MenuItemInfo) obj).menuText;
				mTvItemPort.setText(mAreaName);
				//refreshData();
			}
		}, true, new MenuItemInfo(mTvItemPort.getText().toString()));
		menuPortList.setMenuType(GlobalMessageType.MenuType.SELECT_PORT);
		menuPortList.setTitle(getString(R.string.menu_title_select_port));
		menuPortList.show();
	}

	private void refreshData() {
		String areaCode = mAreaMap.get(mAreaName);
		if (productType == ProductType.SAND) {
			if (!areaCode.equals(mLvSandTodayPrice.getAreaCode())) {
				mLvSandTodayPrice.setAreaCode(areaCode);
				mLvSandTodayPrice.updateDataStatus(DataStatus.LOADING);
				mBuyLogic.getTodayPrice(productType, SysCfgCode.TYPE_PRODUCT_SAND, areaCode);
			}
		} else if (productType == ProductType.STONE) {
			if (!areaCode.equals(mLvStoneTodayPrice.getAreaCode())) {
				mLvStoneTodayPrice.setAreaCode(areaCode);
				mLvStoneTodayPrice.updateDataStatus(DataStatus.LOADING);
				mBuyLogic.getTodayPrice(productType, SysCfgCode.TYPE_PRODUCT_STONE, areaCode);
			}
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.rdoBtn_product_sand:
			if (productType != ProductType.SAND) {
				productType = ProductType.SAND;
				switchView();
			}
			break;

		case R.id.rdoBtn_product_stone:
			if (productType != ProductType.STONE) {
				productType = ProductType.STONE;
				switchView();
			}
			break;

		case R.id.btn_user_login: // 用户登陆
			intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			break;

		case R.id.btn_price_forecast: // 价格预测
			intent = new Intent(this, PriceForecastActivity.class);
			startActivity(intent);
			break;

		case R.id.btn_find_buys: // 找买找卖
			if (getParent() instanceof MainActivity) {
				((MainActivity) getParent()).switchTabView(TabStatus.FIND_BUY);
			}
			break;

		case R.id.btn_my_buys: // 我的供求
			if (checkLoginStatus(true) && getParent() instanceof MainActivity) {
				((MainActivity) getParent()).switchTabView(TabStatus.MY_BUY);
			}
			break;

		case R.id.btn_my_contract: // 我的合同
			if (checkLoginStatus(true) && getParent() instanceof MainActivity) {
				((MainActivity) getParent()).switchTabView(TabStatus.MY_CONTRACT);
			}
			break;

		case R.id.btn_my_purse: // 我的钱包
			if (checkLoginStatus(true) && getParent() instanceof MainActivity) {
				((MainActivity) getParent()).switchTabView(TabStatus.MY_PURSE);
			}
			break;

		case R.id.btn_my_profile: // 我的资料
			if (checkLoginStatus(true) && getParent() instanceof MainActivity) {
				//((MainActivity) getParent()).switchTabView(TabStatus.MY_PROFILE);
				((MainActivity) getParent()).switchTabToMyProfile(false);
			}
			break;

		case R.id.btn_message_center: // 消息盒子
			intent = new Intent(this, MessageListActivity.class);
			startActivity(intent);
			break;

		case R.id.btn_setting: // 帮助
			intent = new Intent(this, SettingActivity.class);
			startActivity(intent);
			break;

		case R.id.ll_port_list: // 港口列表
			showPortListMenu();
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		mPageIndicator.setPageSelectedIndex(position % mPageIndicator.getPageCount());
//		mPageIndicator.setPageSelectedIndex(position);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(GlobalAction.BuyAction.EXTRA_KEY_SELECTED_TAB, productType.toValue());
	}

	@Override
	protected boolean isAddToStack() {
		return false;
	}

	@Override
	public void onItemClick() {
		Intent intent = new Intent(this, PriceForecastActivity.class);
		intent.putExtra(GlobalAction.CommonAction.EXTRA_KEY_SELECTED_TAB, productType.toValue());
		startActivity(intent);
	}

	@Override
	public void onReloadData() {
		if (productType == ProductType.SAND) {
			mLvSandTodayPrice.updateDataStatus(DataStatus.LOADING);
			mBuyLogic.getTodayPrice(productType, SysCfgCode.TYPE_PRODUCT_SAND, mAreaMap.get(mAreaName));
		} else if (productType == ProductType.STONE) {
			mLvStoneTodayPrice.updateDataStatus(DataStatus.LOADING);
			mBuyLogic.getTodayPrice(productType, SysCfgCode.TYPE_PRODUCT_STONE, mAreaMap.get(mAreaName));
		}
	}

	@Override
	protected void initLogics() {
		mBuyLogic = LogicFactory.getLogicByClass(IBuyLogic.class);
		mMessageLogic = LogicFactory.getLogicByClass(IMessageLogic.class);
		mSysCfgLogic = LogicFactory.getLogicByClass(ISysCfgLogic.class);
		iAdvertisingLogic= LogicFactory.getLogicByClass(IAdvertisingLogic.class);
	}

}