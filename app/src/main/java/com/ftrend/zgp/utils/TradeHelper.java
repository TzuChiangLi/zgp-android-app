package com.ftrend.zgp.utils;

import com.ftrend.zgp.model.DepProduct;
import com.ftrend.zgp.model.Trade;
import com.ftrend.zgp.model.TradePay;
import com.ftrend.zgp.model.TradeProd;
import com.ftrend.zgp.model.TradeProd_Table;
import com.ftrend.zgp.model.Trade_Table;
import com.ftrend.zgp.utils.log.LogUtil;
import com.raizlabs.android.dbflow.sql.language.Method;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.FlowCursor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.raizlabs.android.dbflow.sql.language.Method.count;

/**
 * 交易操作类
 * Copyright (C),青岛致远方象软件科技有限公司
 *
 * @author liuhongbin@ftrend.cn
 * @since 2019/9/6
 */
public class TradeHelper {

    // 交易类型：T-销售
    private static final String TRADE_FLAG_SALE = "T";
    // 交易类型：R-退货
    private static final String TRADE_FLAG_RETURN = "R";

    // 交易状态：0-未结
    private static final String TRADE_STATUS_NOTPAY = "0";
    // 交易状态：1-挂起
    private static final String TRADE_STATUS_HANGUP = "1";
    // 交易状态：2-已结
    private static final String TRADE_STATUS_CANCELLED = "2";
    // 交易状态：13-取消
    private static final String TRADE_STATUS_PAID = "3";
    // 交易流水
    private static Trade trade = null;
    // 商品列表
    private static List<TradeProd> prodList = null;
    // 支付信息
    private static TradePay pay = null;

    /**
     * 初始化当前操作的交易流水，读取未结销售流水，不存在则创建新的流水
     */
    public static void initSale() {
        trade = SQLite.select().from(Trade.class)
                .where(Trade_Table.status.eq(TRADE_STATUS_NOTPAY))
                .and(Trade_Table.tradeFlag.eq(TRADE_FLAG_SALE))
                .querySingle();
        if (trade == null) {
            trade = new Trade();
            trade.setDepCode(ZgParams.getCurrentDep().getDepCode());
            trade.setLsNo(newLsNo());
            trade.setTradeTime(null);
            trade.setTradeFlag(TRADE_FLAG_SALE);
            trade.setCashier(ZgParams.getCurrentUser().getUserCode());
            trade.setDscTotal(0);
            trade.setTotal(0);
            trade.setCustType("");
            trade.setVipCode("");
            trade.setCardCode("");
            trade.setVipTotal(0);
            trade.setCreateTime((String.valueOf(LogUtil.getDateTime())));
            trade.setCreateIp(ZgParams.getDevSn().substring(0, 14));
            trade.setStatus(TRADE_STATUS_NOTPAY);

            prodList = new ArrayList<>();

            pay = new TradePay();
            pay.setLsNo(trade.getLsNo());
        }
    }

    /**
     * 添加商品到商品列表
     *
     * @param product
     * @return
     */
    public static long addProduct(DepProduct product) {
        long index = prodList.size();
        TradeProd prod = new TradeProd();
        prod.setLsNo(trade.getLsNo());
        prod.setSortNo(index);
        prod.setProdCode(product.getProdCode());
        prod.setProdName(product.getProdName());
        prod.setBarCode(product.getBarCode());
        prod.setDepCode(product.getDepCode());
        prod.setPrice(product.getPrice());
        prod.setAmount(1);
        prod.setManuDsc(0);
        prod.setVipDsc(0);
        prod.setTranDsc(0);
        prod.setTotal(prod.getPrice() * prod.getAmount());
        prod.setVipDsc(0);
        prod.setSaleInfo("");
        prod.setDelFlag("0");
        if (prod.insert() > 0) {
            prodList.add(prod);
            //重新汇总流水金额
            recalcTotal();
            return index;
        } else {
            return -1;
        }
    }

    /**
     * 完成支付
     *
     * @param payTypeCode 支付类型编号
     * @param amount      支付金额
     * @param change      找零金额
     * @param payCode     支付账号
     * @return
     */
    public static boolean pay(String payTypeCode, float amount, float change, String payCode) {
        if (pay == null) {
            pay = new TradePay();
            pay.setLsNo(trade.getLsNo());
        }
        pay.setPayTypeCode(payTypeCode);
        pay.setAmount(amount);
        pay.setChange(change);
        pay.setPayCode(payCode);
        pay.setPayTime(new Date());
        if (pay.save()) {
            trade.setTradeTime(pay.getPayTime());
            trade.setStatus(TRADE_STATUS_PAID);
            return trade.save();
        } else {
            return false;
        }
    }

    /**
     * 完成支付（仅适用于现金支付）
     *
     * @param payTypeCode
     * @param change
     * @return
     */
    public static boolean pay(String payTypeCode, float change) {
        return pay(payTypeCode, trade.getTotal(), change, "");
    }

    /**
     * 生成新的流水号
     *
     * @return
     */
    private static String newLsNo() {
        FlowCursor cursor = SQLite.select(Method.max(Trade_Table.lsNo)).from(Trade.class).query();
        if (cursor == null || cursor.getCount() == 0 || cursor.isNull(0)) {
            return ZgParams.getPosCode() + "00001";
        } else {
            String lsNo = cursor.getStringOrDefault(0);
            int max = Integer.valueOf(lsNo.substring(2));
            int current = max == 99999 ? 1 : max + 1;
            return ZgParams.getPosCode() + String.format("%05d", current);
        }
    }

    /**
     * 重新汇总流水金额：优惠、合计、积分金额
     */
    private static void recalcTotal() {
        float dscTotal = 0;
        float total = 0;
        float vipTotal = 0;

        for (TradeProd prod : prodList) {
            dscTotal += prod.getManuDsc() + prod.getTranDsc() + prod.getVipDsc();
            total += prod.getTotal();
            vipTotal += prod.getVipTotal();
        }
        trade.setDscTotal(dscTotal);
        trade.setTotal(total);
        trade.setVipTotal(vipTotal);
        trade.save();
    }


    /**
     * 当点击进入收银-选择商品界面但是没有任何操作就退出时
     * 调用此方法，可以清空数据库内的本流水单号记录，并置空常量
     */
    public static void deleteEmptyTrade() {
        long count = SQLite.select(count(TradeProd_Table.id)).from(TradeProd.class).where(TradeProd_Table.lsNo.eq(trade.getLsNo())).count();
        if (count == 0) {
            //删除流水
            SQLite.delete(Trade.class)
                    .where(Trade_Table.lsNo.is(trade.getLsNo()))
                    .async()
                    .execute();
        }
        trade = new Trade();
    }

    public static Trade getTrade() {
        return trade;
    }

}
