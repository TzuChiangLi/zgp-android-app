package com.ftrend.zgp.presenter;

import com.ftrend.zgp.R;
import com.ftrend.zgp.api.Contract;
import com.ftrend.zgp.model.Menu;
import com.ftrend.zgp.model.Trade;
import com.ftrend.zgp.model.Trade_Table;
import com.ftrend.zgp.utils.TradeHelper;
import com.ftrend.zgp.utils.ZgParams;
import com.ftrend.zgp.utils.sunmi.SunmiPayHelper;
import com.ftrend.zgp.utils.task.LsUploadThread;
import com.ftrend.zgp.utils.task.ServerWatcherThread;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.raizlabs.android.dbflow.sql.language.Method.count;

/**
 * 主界面P层----所有业务逻辑在此
 *
 * @author liziqiang@ftrend.cn
 */
public class HomePresenter implements Contract.HomePresenter {
    private Contract.HomeView mView;


    public HomePresenter(Contract.HomeView mView) {
        this.mView = mView;
    }

    public static HomePresenter createPresenter(Contract.HomeView mView) {
        return new HomePresenter(mView);
    }

    @Override
    public void initSunmiPaySdk() {
        SunmiPayHelper.getInstance().connectPayService();
    }

    @Override
    public void initServerThread() {
        //启动后台服务心跳检测线程
        ServerWatcherThread watcherThread = new ServerWatcherThread();
        watcherThread.start();
        //启动数据上传线程
        LsUploadThread lsUploadThread = new LsUploadThread();
        lsUploadThread.start();
    }

    @Override
    public void initMenuList() {
        List<Menu> menuList = new ArrayList<>();
        List<Menu.MenuList> childList = new ArrayList<>();
        String[] menuName = {"收银", "取单", "退货", "交班", "交班报表", "交易统计", "流水查询", "数据同步", "操作指南", "参数设置"
                , "修改密码", "注销登录"};
        int[] menuImg = {R.drawable.jy_sy, R.drawable.jy_qd, R.drawable.jy_th, R.drawable.jy_jb, R.drawable.jy_sy, R.drawable.jy_qd, R.drawable.jy_th, R.drawable.jy_jb,
                R.drawable.jy_sy, R.drawable.jy_qd, R.drawable.jy_th, R.drawable.jy_jb};
        for (int i = 0; i < 4; i++) {
            childList.add(new Menu.MenuList(menuImg[i], menuName[i]));
        }
        menuList.add(new Menu("交易", childList));
        childList = new ArrayList<>();
        for (int i = 4; i < 7; i++) {
            childList.add(new Menu.MenuList(menuImg[i], menuName[i]));
        }
        menuList.add(new Menu("报表查询", childList));
        childList = new ArrayList<>();
        for (int i = 7; i < menuName.length; i++) {
            childList.add(new Menu.MenuList(menuImg[i], menuName[i]));
        }
        menuList.add(new Menu("系统功能", childList));
        mView.setMenuList(menuList);

    }

    @Override
    public void setInfo() {
        int size = 3;
        String[] info = new String[size];
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        Arrays.fill(info, sdf.format(new Date()));
        mView.showInfo(info);
    }

    @Override
    public void goShopCart() {
        //初始化流水单信息
        TradeHelper.initSale();
        mView.goShopChartActivity(TradeHelper.getTrade().getLsNo());
    }

    @Override
    public void goHandover() {
        if (ZgParams.isIsOnline()) {
            long tradeCount = SQLite.select(count()).from(Trade.class)
                    .where(Trade_Table.status.eq(TradeHelper.TRADE_STATUS_PAID)).count();
            if (tradeCount > 0) {
                mView.goHandoverActivity();
            } else {
                mView.hasNoTrade();
            }
        } else {
            mView.showOfflineTip();
        }


    }

    @Override
    public void getOutOrder() {
        mView.goOrderOutActivity();
    }

    @Override
    public void logout() {
        //清除登录信息
        ZgParams.clearCurrentInfo();

        mView.logout();
    }

    @Override
    public void onDestory() {
        SunmiPayHelper.getInstance().disconnectPayService();
        if (mView != null) {
            mView = null;
        }
    }

}
