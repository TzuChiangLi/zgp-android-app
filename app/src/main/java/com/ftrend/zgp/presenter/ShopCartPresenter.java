package com.ftrend.zgp.presenter;

import com.blankj.utilcode.util.StringUtils;
import com.ftrend.zgp.api.ShopCartContract;
import com.ftrend.zgp.model.DepCls;
import com.ftrend.zgp.model.DepCls_Table;
import com.ftrend.zgp.model.DepProduct;
import com.ftrend.zgp.model.DepProduct_Table;
import com.ftrend.zgp.model.Product;
import com.ftrend.zgp.model.Product_Table;
import com.ftrend.zgp.model.VipInfo;
import com.ftrend.zgp.utils.DscHelper;
import com.ftrend.zgp.utils.TradeHelper;
import com.ftrend.zgp.utils.ZgParams;
import com.ftrend.zgp.utils.http.RestBodyMap;
import com.ftrend.zgp.utils.http.RestCallback;
import com.ftrend.zgp.utils.http.RestResultHandler;
import com.ftrend.zgp.utils.http.RestSubscribe;
import com.ftrend.zgp.utils.log.LogUtil;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 收银-选择商品P层
 *
 * @author liziqiang@ftrend.cn
 */
public class ShopCartPresenter implements ShopCartContract.ShopCartPresenter {
    private static final String TAG = "ShopCartPresenter";
    private ShopCartContract.ShopCartView mView;
    private List<Product> mProdList = new ArrayList<>();

    //查询参数
    private int mPageSize = 20;//每页行数
    private int mPage = 0;//当前页
    private String mClassCode = null;//当前分类
    private String mQueryStr = null;//查询字符串，用于扫码或模糊查询

    private ShopCartPresenter(ShopCartContract.ShopCartView mView) {
        this.mView = mView;
    }

    public static ShopCartPresenter createPresenter(ShopCartContract.ShopCartView mView) {
        return new ShopCartPresenter(mView);
    }

    @Override
    public void refreshTrade() {
        TradeHelper.initSale();
    }

    /**
     * 生成季节查询条件
     *
     * @return
     */
    public static String makeSeanFilter() {
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        if (month <= 3) {
            return "1___";
        } else if (month <= 6) {
            return "_1__";
        } else if (month <= 9) {
            return "__1_";
        } else {
            return "___1";
        }
    }

    @Override
    public void initProdList() {
        /*//注意：状态为注销、暂停销售的商品，以及季节商品在商品列表不显示；但已经添加到购物车的不受影响
        List<DepProduct> mTempList = SQLite.select(DepProduct_Table.prodCode)
                .from(DepProduct.class)
                .where(DepProduct_Table.depCode.eq(ZgParams.getCurrentDep().getDepCode()))
                .queryList();
        List<String> mStrList = new ArrayList<>();
        for (DepProduct prod : mTempList) {
            mStrList.add(prod.getProdCode());
        }
        mProdList = SQLite.select().from(Product.class)
                .where(Product_Table.prodCode.in(mStrList))
                .and(Product_Table.prodStatus.withTable().notIn("2", "3"))
                //季节销售商品
                .and(OperatorGroup.clause(Product_Table.season.withTable().eq("0000"))
                        .or(Product_Table.season.withTable().like(makeSeanFilter())))
                .queryList();
        for (Product product : mProdList) {
            product.setSelect(false);
        }
        mView.setProdList(mProdList);*/
        mProdList = new ArrayList<>();
        mView.setProdList(mProdList);
        loadPage();
        if (ZgParams.isShowCls(ZgParams.getCurrentDep().getDepCode())) {
            List<DepCls> clsList = SQLite.select().from(DepCls.class)
                    .where(DepCls_Table.depCode.eq(ZgParams.getCurrentDep().getDepCode()))
                    .queryList();
            for (DepCls cls : clsList) {
                cls.setSelect(false);
            }
            DepCls depCls = new DepCls();
            depCls.setClsName("全部类别");
            depCls.setDepCode("all");
            depCls.setClsCode("all");
            depCls.setSelect(true);

            clsList.add(0, depCls);
            mView.setClsList(clsList);
        }
    }

    @Override
    public void loadMoreProd() {
        mPage++;
        loadPage();
    }

    private void loadPage() {
        //注意：状态为注销、暂停销售的商品，以及季节商品在商品列表不显示；但已经添加到购物车的不受影响
        IProperty[] selectColumns = Product_Table.ALL_COLUMN_PROPERTIES;
        //给字段名加上表名，防止查询语句字段名冲突
        for (int i = 0; i < selectColumns.length; i++) {
            selectColumns[i] = selectColumns[i].withTable();
        }
        Where<Product> where = SQLite.select(selectColumns).from(Product.class)
                .join(DepProduct.class, Join.JoinType.INNER)
                .on(Product_Table.prodCode.withTable().eq(DepProduct_Table.prodCode.withTable()),
                        DepProduct_Table.depCode.withTable().eq(ZgParams.getCurrentDep().getDepCode()))
                .where(Product_Table.prodStatus.withTable().notIn("2", "3"))
                //季节销售商品
                .and(OperatorGroup.clause(Product_Table.season.withTable().eq("0000"))
                        .or(Product_Table.season.withTable().like(makeSeanFilter())));
        //过滤条件：分类
        if (!StringUtils.isEmpty(mClassCode) && !"all".equalsIgnoreCase(mClassCode)) {
            where = where.and(Product_Table.clsCode.withTable().eq(mClassCode));
        }
        //过滤条件：模糊匹配字符串
        if (!StringUtils.isEmpty(mQueryStr)) {
            String filter = "%" + mQueryStr + "%";
            where = where.and(OperatorGroup.clause(Product_Table.prodCode.withTable().like(filter))
                    .or(Product_Table.prodName.withTable().like(filter))
                    .or(Product_Table.barCode.withTable().like(filter))
            );
        }
        mProdList = where.offset(mPage * mPageSize).limit(mPageSize).queryList();
        for (Product product : mProdList) {
            product.setSelect(false);
        }
        //page = 0，更新商品列表；否则，追加到商品列表
        if (mPage == 0) {
            mView.updateProdList(mProdList);
        } else {
            mView.appendProdList(mProdList);
        }
    }

    @Override
    public void initOrderInfo() {
        mView.updateTradeProd(TradeHelper.getTradeCount(), TradeHelper.getTradeTotal());
    }

    @Override
    public void updateOrderInfo() {
        mView.updateOrderInfo();
        mView.updateTradeProd(TradeHelper.getTradeCount(), TradeHelper.getTradeTotal());
    }


    @Override
    public void searchProdList(String... key) {
        mClassCode = key[0];
        mQueryStr = key[1];
        mPage = 0;
        loadPage();
        //key[0]==clsCode
        //key[1]==输入关键字
        /*List<Product> fliterList = new ArrayList<>();
        if (!TextUtils.isEmpty(key[0])) {
            if ("all".equals(key[0]) && TextUtils.isEmpty(key[1])) {
                mView.updateProdList(mProdList);
                return;
            }
            if (mProdList != null && mProdList.size() != 0) {
                for (Product product : mProdList) {
                    if (!"all".equals(key[0])) {
                        //有类别编码
                        if (!TextUtils.isEmpty(product.getClsCode())) {
                            //该分类下
                            if (product.getClsCode().contains(key[0])) {
                                //符合关键词筛选
                                if (!TextUtils.isEmpty(product.getProdCode())) {
                                    if (product.getProdCode().contains(key[1])) {
                                        fliterList.add(product);
                                        continue;
                                    }
                                }
                                if (!TextUtils.isEmpty(product.getProdName())) {
                                    if (product.getProdName().contains(key[1])) {
                                        fliterList.add(product);
                                        continue;
                                    }
                                }
                                if (!TextUtils.isEmpty(product.getBarCode())) {
                                    if (product.getBarCode().contains(key[1])) {
                                        fliterList.add(product);
                                    }
                                }
                            }
                        }
                    } else {
                        if (!TextUtils.isEmpty(product.getProdCode())) {
                            if (product.getProdCode().contains(key[1])) {
                                fliterList.add(product);
                                continue;
                            }
                        }
                        if (!TextUtils.isEmpty(product.getProdName())) {
                            if (product.getProdName().contains(key[1])) {
                                fliterList.add(product);
                                continue;
                            }
                        }
                        if (!TextUtils.isEmpty(product.getBarCode())) {
                            if (product.getBarCode().contains(key[1])) {
                                fliterList.add(product);
                            }
                        }
                    }
                }
            }
            mView.updateProdList(fliterList);
        } else {
            //防止意外情况
            if (TextUtils.isEmpty(key[1])) {
                //如果关键词为空
                mView.updateProdList(mProdList);
            } else {
                //关键词不为空
                for (Product product : mProdList) {
                    if (!TextUtils.isEmpty(product.getProdCode())) {
                        if (product.getProdCode().contains(key[1])) {
                            fliterList.add(product);
                            continue;
                        }
                    }
                    if (!TextUtils.isEmpty(product.getProdName())) {
                        if (product.getProdName().contains(key[1])) {
                            fliterList.add(product);
                            continue;
                        }
                    }
                    if (!TextUtils.isEmpty(product.getBarCode())) {
                        if (product.getBarCode().contains(key[1])) {
                            fliterList.add(product);
                        }
                    }
                }
                mView.updateProdList(fliterList);
            }

        }*/
    }

    @Override
    public void addToShopCart(Product product) {
        addToShopCart(product, product.getPrice());
    }

    @Override
    public void addToShopCart(Product product, double price) {
        if (TradeHelper.addProduct(product) == -1) {
            LogUtil.e("向数据库添加商品失败");
        } else {
            TradeHelper.priceChangeInShopCart(price);
            if (TradeHelper.vip != null) {
                //如果会员已登录且有会员信息
                DscHelper.saveVipProdDsc(TradeHelper.getProdList().size() - 1);
            } else {
                //如果单据未结，此时退出重新进入，此时vip==null
                if (ZgParams.isIsOnline()) {
                    RestSubscribe.getInstance().queryVipInfo(TradeHelper.getTrade().getVipCode(), new RestCallback(regHandler));
                } else {
                    if (TradeHelper.vip != null) {
                        mView.showError("无法获取会员优惠信息");
                    }
                }
            }
            updateTradeInfo();
        }
    }

    private RestResultHandler regHandler = new RestResultHandler() {
        @Override
        public void onSuccess(RestBodyMap body) {
            VipInfo vipInfo = TradeHelper.vip();
            vipInfo.setVipName(body.getString("vipName"));
            vipInfo.setVipCode(body.getString("vipCode"));
            vipInfo.setVipDscRate(body.getDouble("vipDscRate"));
            vipInfo.setVipGrade(body.getString("vipGrade"));
            vipInfo.setVipPriceType(body.getDouble("vipPriceType"));
            vipInfo.setRateRule(body.getDouble("rateRule"));
            vipInfo.setForceDsc(body.getString("forceDsc"));
            vipInfo.setCardCode(body.getString("cardCode"));
            vipInfo.setDscProdIsDsc(body.getString("dscProdIsDsc"));
            //保存会员信息
            TradeHelper.saveVip();
            //会员优惠
            DscHelper.saveVipProdDsc(TradeHelper.getProdList().size() - 1);
        }

        @Override
        public void onFailed(String errorCode, String errorMsg) {
        }
    };

    @Override
    public void setTradeStatus(String status) {
        TradeHelper.setTradeStatus(status);
        TradeHelper.saveVipInfo();
        mView.returnHomeActivity(TradeHelper.convertTradeStatus(status));
        TradeHelper.clear();
        TradeHelper.clearVip();

    }

    @Override
    public void cancelPriceChange(int index) {
        TradeHelper.rollackPriceChangeInShopCart();
        mView.cancelAddProduct(index);
        updateTradeInfo();
    }

    @Override
    public void updateTradeInfo() {
        double price = TradeHelper.getTradeTotal();
        long count = TradeHelper.getTradeCount();
        mView.updateTradeProd(count, price);
    }

    @Override
    public void searchProdByScan(String code, List<Product> prodList) {
        /*for (int i = 0; i < prodList.size(); i++) {
            if (code.equals(prodList.get(i).getProdCode()) ||
                    code.equals(prodList.get(i).getBarCode())) {
                mView.setScanProdPosition(i);
                break;
            }
            if (i == prodList.size() - 1) {
                mView.noScanProdPosition();
            }
        }*/
        searchProdList(null, code);
        if (mProdList.size() > 0) {
            mView.setScanProdPosition(0);
        } else {
            mView.noScanProdPosition();
        }
    }

    @Override
    public void onDestory() {
        if (mView != null) {
            mView = null;
            mProdList = null;
            System.gc();
        }
    }
}
