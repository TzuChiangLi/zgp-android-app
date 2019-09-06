package com.ftrend.zgp.utils.task;

import android.util.Log;

import com.ftrend.zgp.model.Trade;
import com.ftrend.zgp.model.TradePay;
import com.ftrend.zgp.model.TradePay_Table;
import com.ftrend.zgp.model.TradeProd;
import com.ftrend.zgp.model.TradeProd_Table;
import com.ftrend.zgp.model.TradeUploadQueue;
import com.ftrend.zgp.model.TradeUploadQueue_Table;
import com.ftrend.zgp.model.Trade_Table;
import com.ftrend.zgp.utils.ZgParams;
import com.ftrend.zgp.utils.http.RestCallback;
import com.ftrend.zgp.utils.http.RestResultHandler;
import com.ftrend.zgp.utils.http.RestSubscribe;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 流水上传线程
 * Copyright (C),青岛致远方象软件科技有限公司
 *
 * @author liuhongbin@ftrend.cn
 * @since 2019/9/6
 */
public class LsUploadThread extends Thread {
    private final String TAG = "LsUploadThread";

    //是否正在上传数据，防止重复调用
    private boolean isUploading = false;

    public void run() {
        super.run();

        String posCode = ZgParams.getPosCode();
        while (!isInterrupted()) {
            List<TradeUploadQueue> list = SQLite.select().from(TradeUploadQueue.class)
                    .where(TradeUploadQueue_Table.uploadTime.isNull())
                    .queryList();
            if (list.size() == 0 || !ZgParams.isIsOnline()) {
                //没有需要上传的数据或者单机模式，等待一段时间
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    break;
                }
                continue;
            }

            for (final TradeUploadQueue queue : list) {
                if (isInterrupted()) {
                    break;//线程终止，停止上传
                }

                String lsNo = queue.getLsNo();
                Trade trade = SQLite.select().from(Trade.class)
                        .where(Trade_Table.lsNo.eq(lsNo))
                        .querySingle();
                List<TradeProd> prodList = SQLite.select().from(TradeProd.class)
                        .where(TradeProd_Table.lsNo.eq(lsNo))
                        .queryList();
                TradePay pay = SQLite.select().from(TradePay.class)
                        .where(TradePay_Table.lsNo.eq(lsNo))
                        .querySingle();


                isUploading = true;
                RestSubscribe.getInstance().uploadTrade(posCode, trade, prodList, pay,
                        new RestCallback(new RestResultHandler() {
                            @Override
                            public void onSuccess(Map<String, Object> body) {
                                //记录上传时间
                                queue.setUploadTime(new Date());
                                queue.update();
                                isUploading = false;
                            }

                            @Override
                            public void onFailed(String errorCode, String errorMsg) {
                                Log.e(TAG, "流水上传失败: " + errorCode + " - " + errorMsg);
                                isUploading = false;//上传失败不做处理，下次再上传
                            }
                        }));

                while (isUploading) {
                    //等待上传完成
                    yield();
                    if (isInterrupted()) {
                        break;//线程终止，停止等待
                        //此时退出可能导致流水重复上传（未标记已上传），后台服务有对应的处理机制
                    }
                }
            }
        }
    }


}
