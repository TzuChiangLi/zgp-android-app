package com.ftrend.zgp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ftrend.zgp.R;
import com.ftrend.zgp.adapter.ShopAdapter;
import com.ftrend.zgp.api.RtnProdContract;
import com.ftrend.zgp.base.BaseActivity;
import com.ftrend.zgp.model.Product;
import com.ftrend.zgp.model.TradeProd;
import com.ftrend.zgp.presenter.RtnProdPresenter;
import com.ftrend.zgp.utils.ZgParams;
import com.ftrend.zgp.utils.common.ClickUtil;
import com.ftrend.zgp.utils.log.LogUtil;
import com.ftrend.zgp.utils.msg.InputPanel;
import com.ftrend.zgp.utils.msg.MessageUtil;
import com.ftrend.zgp.utils.pop.MoneyInputCallback;
import com.ftrend.zgp.utils.pop.ProdDialog;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.bar.OnTitleBarListener;
import com.hjq.bar.TitleBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 退货----包含按单退货和不按单退货
 *
 * @author liziqiang@ftrend.cn
 */
public class RtnProdActivity extends BaseActivity implements OnTitleBarListener, RtnProdContract.RtnProdView {
    @BindView(R.id.rtn_prod_top_bar)
    TitleBar mTitleBar;
    @BindView(R.id.rtn_prod_top_bar_title)
    TextView mTitleTv;
    @BindView(R.id.rtn_prod_tv_total)
    TextView mTotalTv;
    @BindView(R.id.rtn_prod_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.rtn_prod_rl_bottom_prod)
    RelativeLayout mProdLayout;
    private int oldPosition = -1, oldDepIndex = -1;
    private RecyclerView mDepRecyclerView;
    private RtnProdContract.RtnProdPresenter mPresenter;
    private ShopAdapter<TradeProd> mProdAdapter;
    private ShopAdapter<Product> mDepAdapter;
    private List<Product> prodList;

    @Override
    public void onNetWorkChange(boolean isOnline) {
        if (mTitleBar == null) {
            mTitleBar = findViewById(R.id.rtn_prod_top_bar);
        }
        mTitleBar.setRightIcon(isOnline ? R.drawable.online : R.drawable.offline);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.rtn_prod_activity;
    }

    @Override
    protected void initData() {
        mPresenter.updateTradeInfo();
        if (mProdAdapter == null) {
            //刷新不按单退货的界面
            mProdAdapter = new ShopAdapter<>(R.layout.shop_list_rv_product_item, null, 8);
            //取消item刷新动画，不会闪那一下
            ((SimpleItemAnimator) (Objects.requireNonNull(mRecyclerView.getItemAnimator())))
                    .setSupportsChangeAnimations(false);
        }
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mProdAdapter);
        mProdAdapter.setEmptyView(getLayoutInflater().inflate(R.layout.rv_item_empty,
                (ViewGroup) mRecyclerView.getParent(), false));
    }

    @Override
    protected void initView() {
        if (mPresenter == null) {
            mPresenter = RtnProdPresenter.createPresenter(this);
        }
    }

    @Override
    protected void initTitleBar() {
        ImmersionBar.with(this).fitsSystemWindows(true).statusBarColor(R.color.common_white)
                .autoDarkModeEnable(true).init();
        mTitleBar.setRightIcon(ZgParams.isIsOnline() ? R.drawable.online : R.drawable.offline);
        mTitleBar.setOnTitleBarListener(this);
    }

    @Override
    public void onLeftClick(View v) {
        finish();
    }

    @Override
    public void onTitleClick(View v) {
    }

    @Override
    public void onRightClick(View v) {
    }

    @Override
    public void setPresenter(RtnProdContract.RtnProdPresenter presenter) {
        if (presenter != null) {
            mPresenter = presenter;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            switch (requestCode) {
                case SCAN_SUNMI:
                    //商米扫描
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        ArrayList result = null;
                        if (bundle != null) {
                            result = (ArrayList) bundle.getSerializable("data");
                        }
                        if (result != null) {
                            for (Object o : result) {
                                HashMap hashMap = (HashMap) o;
                                //此处传入扫码结果
                                scanResult(String.valueOf(hashMap.get("VALUE")));
                                //                Log.i("----sunmi", String.valueOf(hashMap.get("TYPE")));//这个是扫码的类型
                                //                Log.i("----sunmi", String.valueOf(hashMap.get("VALUE")));//这个是扫码的结果
                            }
                        }
                    }
                    break;
                case SCAN_CAMERA:
                    //普通相机扫描
                    if (data != null && resultCode == ScanActivity.SCAN_OK) {
                        String scanCode = data.getStringExtra("scanResult");
                        scanResult(scanCode);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LogUtil.e(e.getMessage());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanResult(String value) {
        if (TextUtils.isEmpty(value)) {
            return;
        }
        if (prodList != null) {
            mPresenter.searchProdByScan(value, prodList);
        } else {
            MessageUtil.show("商品列表为空");
        }
    }

    @OnClick(R.id.rtn_prod_btn_prod_rtn)
    public void pay() {
        if (ClickUtil.onceClick()) {
            return;
        }
        if (!mProdAdapter.getData().isEmpty()) {
            Intent intent = new Intent(RtnProdActivity.this, PayActivity.class);
            intent.putExtra("isSale", false);
            startActivity(intent);
        } else {
            MessageUtil.showError("当前无退货商品");
        }

    }

    @OnClick(R.id.rtn_prod_btn_prod_add)
    public void add() {
        if (ClickUtil.onceClick()) {
            return;
        }
        mPresenter.showRtnProdDialog();
    }

    @OnClick(R.id.rtn_prod_btn_back)
    public void back() {
        if (ClickUtil.onceClick()) {
            return;
        }
        finish();
    }

    @Override
    public void showRtnProdDialog(final List<Product> mProdList) {
        prodList = mProdList;
        //不按单退货添加退货商品
        MessageUtil.rtnProd(new ProdDialog.onDialogCallBack() {
            @Override
            public void onLoad(RecyclerView recyclerView, final ShopAdapter<Product> mAdapter) {
                mDepAdapter = mAdapter;
                mDepRecyclerView = recyclerView;
                mDepRecyclerView.setLayoutManager(new LinearLayoutManager(RtnProdActivity.this));
                mDepAdapter.setNewData(mProdList);
                mDepRecyclerView.addItemDecoration(new DividerItemDecoration(RtnProdActivity.this, DividerItemDecoration.VERTICAL));
                mDepRecyclerView.setAdapter(mDepAdapter);
                //设置分页，上拉加载更多
                mDepAdapter.setOnLoadMoreListener(loadMoreListener, mDepRecyclerView);
                mDepAdapter.disableLoadMoreIfNotFullPage();
            }

            @Override
            public void onItemClick(RecyclerView view, ShopAdapter<Product> adapter, final int position) {
                if (ClickUtil.onceClick()) {
                    return;
                }
                if (oldDepIndex != -1 && oldDepIndex < mDepAdapter.getItemCount()) {
                    mProdList.get(oldDepIndex).setSelect(false);
                    mDepAdapter.notifyItemChanged(oldDepIndex);
                }
                oldDepIndex = position;
                final Product prod = mDepAdapter.getItem(position);
                prod.setSelect(true);
                mDepAdapter.notifyItemChanged(position);
                if (prod.getPrice() == 0 && prod.getPriceFlag() == 1) {
                    //修改价格
                    InputPanel.showPriceChange(RtnProdActivity.this, new MoneyInputCallback() {
                        @Override
                        public void onOk(double value) {
                            //添加到购物车中
                            mPresenter.addRtnProd(prod, value);
                        }

                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public String validate(double value) {
                            return null;
                        }
                    });
                } else {
                    //添加到购物车中
                    mPresenter.addRtnProd(mDepAdapter.getItem(position));
                }
            }

            @Override
            public void onScan() {
                try {
                    scan();
                } catch (Exception e) {
                    MessageUtil.showError("本设备不支持扫码");
                }
            }

            @Override
            public void onClose() {
                //需要刷新界面，已经自带关闭弹窗，这里只需要加入除关闭之外的操作
                if (mProdAdapter != null) {
                    mPresenter.updateRtnProdList();
                }
            }

            @Override
            public void onSearch(String key, ShopAdapter<Product> mAdapter) {
                //需要过滤商品
                List<Product> products = mPresenter.searchDepProdList(key, mProdList);
                if (products != null && !products.isEmpty()) {
                    mAdapter.setNewData(products);
                } else {
                    mAdapter.setNewData(null);
                }
            }

            BaseQuickAdapter.RequestLoadMoreListener loadMoreListener = new BaseQuickAdapter.RequestLoadMoreListener() {
                @Override
                public void onLoadMoreRequested() {
                    mDepRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.loadMoreProd();
                        }
                    }, 1000);
                }
            };
        });
    }

    @Override
    public void updateProdList(List<Product> prodList) {
        if (prodList.size() != 0) {
            mDepAdapter.replaceData(prodList);
            mDepAdapter.notifyDataSetChanged();
        } else {
            mDepAdapter.setNewData(null);
            mDepAdapter.setEmptyView(getLayoutInflater().inflate(R.layout.rv_item_empty, (ViewGroup) mDepRecyclerView.getParent(), false));
        }
    }

    @Override
    public void appendProdList(List<Product> prodList) {
        if (prodList == null || prodList.size() == 0) {
            mDepAdapter.loadMoreEnd();
        } else {
            mDepAdapter.addData(prodList);
            mDepAdapter.loadMoreComplete();
        }
    }

    @Override
    public void initProdList(List<TradeProd> prodList) {
        mProdAdapter.setNewData(prodList);
        mProdAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, final int position) {
                if (ClickUtil.onceClick()) {
                    return;
                }
                switch (view.getId()) {
                    case R.id.shop_list_rv_img_add:
                        mPresenter.checkInputNum(position, 1);
                        break;
                    case R.id.shop_list_rv_img_minus:
                        mPresenter.changeAmount(position, -1);
                        break;
                    case R.id.shop_list_rv_btn_change_price:
                        showInputPanel(position);
                        break;
                    case R.id.shop_list_rv_btn_del:
                        MessageUtil.question("确定删除此商品？", new MessageUtil.MessageBoxYesNoListener() {
                            @Override
                            public void onYes() {
                                mPresenter.delRtnProd(position);
                            }

                            @Override
                            public void onNo() {
                            }
                        });
                        break;
                    default:
                        break;
                }
            }
        });
        mProdAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (ClickUtil.onceClick()) {
                    return;
                }
                if (oldPosition != -1 && oldPosition < adapter.getItemCount()) {
                    mProdAdapter.getData().get(oldPosition).setSelect(false);
                    mProdAdapter.notifyItemChanged(oldPosition);
                }
                oldPosition = position;
                mProdAdapter.getData().get(position).setSelect(true);
                mProdAdapter.notifyItemChanged(position);
                mRecyclerView.smoothScrollToPosition(position);
            }
        });
    }

    @Override
    public void delTradeProd(int index) {
        mProdAdapter.notifyItemRemoved(index);
    }

    @Override
    public void setScanProdPosition(int index) {
        if (mDepRecyclerView != null) {
            mDepRecyclerView.smoothScrollToPosition(index);
            if (oldDepIndex != -1 && oldDepIndex < mDepAdapter.getItemCount()) {
                mDepAdapter.getData().get(oldDepIndex).setSelect(false);
                mDepAdapter.notifyItemChanged(oldDepIndex);
            }
            oldDepIndex = index;
            final Product prod = mDepAdapter.getData().get(index);
            prod.setSelect(true);
            if (prod.getPrice() == 0 && prod.getPriceFlag() == 1) {
                //修改价格
                InputPanel.showPriceChange(RtnProdActivity.this, new MoneyInputCallback() {
                    @Override
                    public void onOk(double value) {
                        //添加到购物车中
                        mPresenter.addRtnProd(prod, value);
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public String validate(double value) {
                        return null;
                    }
                });
            } else {
                //添加到购物车中
                mPresenter.addRtnProd(mDepAdapter.getItem(index));
            }
            mDepAdapter.notifyItemChanged(index);
            MessageUtil.show("添加成功");
        }
    }


    @Override
    public void showInputPanel(final int position) {
        InputPanel.showPriceChange(RtnProdActivity.this, new MoneyInputCallback() {
            @Override
            public void onOk(double value) {
                mPresenter.changePrice(position, value);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public String validate(double value) {
                return null;
            }
        });
    }


    @Override
    public void updateTradeProd(int index) {
        mProdAdapter.notifyItemChanged(index);
    }

    @Override
    public void showRtnTotal(double rtnTotal) {
        mTotalTv.setText(String.format(Locale.CHINA, "%.2f", rtnTotal).replace("-", ""));
    }

    @Override
    public void showInputNumDialog(final int index) {
        InputPanel.showInputNumDialog(RtnProdActivity.this, new MoneyInputCallback() {
            @Override
            public void onOk(double value) {
                if (value == 0) {
                    //输入为0时提示是否行清
                    MessageUtil.question("确定删除此商品？", new MessageUtil.MessageBoxYesNoListener() {
                        @Override
                        public void onYes() {
                            mPresenter.delRtnProd(index);
                        }

                        @Override
                        public void onNo() {
                        }
                    });
                } else {
                    //写入数量更新界面
                    mPresenter.coverAmount(index, value);
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public String validate(double value) {
                return null;
            }
        });
    }


    @Override
    public void showError(String msg) {
        MessageUtil.showError(msg);
    }

    @Override
    public void showSuccess(String msg) {
        MessageUtil.showSuccess(msg);
    }


    @Override
    protected void onDestroy() {
        mProdAdapter = null;
        mDepAdapter = null;
        prodList = null;
        mPresenter.onDestory();
        super.onDestroy();
    }
}
