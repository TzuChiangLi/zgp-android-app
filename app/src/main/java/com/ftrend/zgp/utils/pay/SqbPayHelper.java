package com.ftrend.zgp.utils.pay;

import com.ftrend.zgp.model.SqbPayOrder;
import com.ftrend.zgp.model.SqbPayResult;
import com.ftrend.zgp.model.Trade;
import com.ftrend.zgp.utils.TradeHelper;
import com.ftrend.zgp.utils.ZgParams;
import com.ftrend.zgp.utils.common.CommonUtil;
import com.ftrend.zgp.utils.log.LogUtil;
import com.wosai.upay.bean.UpayOrder;
import com.wosai.upay.bean.UpayResult;
import com.wosai.upay.common.UpayCallBack;
import com.wosai.upay.common.UpayTask;

import java.util.Date;

/**
 * 收钱吧API接口功能类
 * Copyright (C),青岛致远方象软件科技有限公司
 *
 * @author liuhongbin@ftrend.cn
 * @since 2019/10/11
 */
public class SqbPayHelper {
    /**
     * 收钱吧接入域名
     */
    public static final String apiDomain = "https://api.shouqianba.com";
    /**
     * 服务商ID
     */
    public static final String vendorId = "20160407103537";
    /**
     * 服务商Key
     */
    public static final String vendorKey = "a8bbaa0aeb3daf40f5924a3a9b694d00";
    /**
     * 服务商AppId
     */
    public static String appId = "";//ftrend.zgpos

    /**
     * 终端号
     */
    public static String terminalSn = "100035370009266786";
    /**
     * 终端密钥
     */
    public static String terminalKey = "316849581b75841b8b3f61f7cac0194f";
// "terminal_sn":"100035370009266786","terminal_key":"316849581b75841b8b3f61f7cac0194f",
// "merchant_sn":"1680000635494","merchant_name":"青岛方象",
// "store_sn":"1580000002217123","store_name":"方象测试"
    /**
     * 是否打开交易完成时,成功和失败的提示声音
     */
    public static boolean playSound = false;

    /**
     * 支付结果回调
     */
    public interface PayResultCallback {
        /**
         * 支付结果
         *
         * @param isDone    是否已完成支付（支付平台处理延迟，接口会返回中间状态，此时需要轮询支付结果）
         * @param isSuccess 是否支付成功
         * @param errMsg    错误消息
         */
        void onResult(boolean isDone, boolean isSuccess, String errMsg);
    }

    /*
如果 激活 接口 result_code返回的不是 ACTIVATE_SUCCESS，则认为激活失败
如果 付款 接口 result_code返回的不是 PAY_SUCCESS，则认为付款失败
如果 退款 接口 result_code返回的不是 REFUND_SUCCESS，则认为退款失败
如果 撤单 接口 result_code返回的不是 CANCEL_SUCCESS 或者 CANCEL_ABORT_SUCCESS 则认为撤单失败
如果 预下单 接口 result_code返回的不是 PRECREATE_SUCCESS，则认为预下单失败
如果 查询 接口 result_code返回的不是 SUCCESS，则认为查询失败
如果 result_code 返回值为 null 或者 0，通过 error_code 和 error_message 信息处理。
如果 result_code 返回的是失败响应码,可先通过 error_code 和 error_message 信息处理,如果不存在 error_code 和 error_message,再根据 result_code 的含义及下一步处理。
     */

    /**
     * 设备激活
     */
    public static void activate() {
        if (UpayTask.getInstance().isActivated()) {
            return;
        }
        String activateCode = "16060878";
        String deviceSn = "FT0001";
        String deviceName = "ZGPOS001";
        /**
         * activate 激活
         * @param code 激活码内容
         * @param vendor_id 服务商 ID
         * @param vendor_key 服务商 KEY
         * @param appId 服务商应用ID
         * @param deviceSn 设备号
         * @param deviceName 设备名称
         * @param payModel 支付模式
         * @param callBack 请求回调
         */
        UpayTask.getInstance().activate(activateCode, vendorId, vendorKey, UpayOrder.PayModel.NO_UI,
                new UpayCallBack() {
                    @Override
                    public void onExecuteResult(UpayResult result) {
                        LogUtil.e(result.toString());
                    }
                });
        //appId, deviceSn, deviceName,
    }

    /*
    https://api.shouqianba.com/upay/v2/pay
    {"terminal_sn":"100035370009271610","client_sn":"20112019101530100003","total_amount":"1",
     "dynamic_id":"288868636158142336","subject":"交易测试","operator":"508","description":"购物",
     "longitude":"4.9E-324","latitude":"4.9E-324","device_id":"1F0070AA33AB6AE195D74BC1899F3EA1",
     "reflect":"30100003"}
     Authorization:100035370009271610 100c09a03bce88e8df413808296a8606
     Content-Type:application/json

    UpayResult
    {terminal_sn='null', terminal_key='null', sn='7895238408655453', client_sn='client8673787',
     trade_no='242019101522001493110554510275', status='SUCCESS', order_status='PAID', payway='1',
     sub_payway='1', qr_code='null', payer_uid='2088002572693112', payer_login='red***@sina.com',
     total_amount='100', net_amount='100', subject='购物', finish_time='1571129271594',
     channel_finish_time='1571129271000', operator='508', description='零售商品', reflect='30100003',
     refund_request_no='null', result_code='PAY_SUCCESS', error_code='', error_message=''}
     */

    /**
     * 支付请求
     *
     * @param scanCode 用户支付码内容（扫描的支付条码或二维码）
     * @param callback 支付结果回调
     */
    public static void pay(String scanCode, final PayResultCallback callback) {
        Trade trade = TradeHelper.getTrade();
        trade.setTradeTime(new Date());
        //String clientSn = trade.getDepCode() + CommonUtil.dateToString(trade.getTradeTime()) + trade.getLsNo();
        String clientSn = "client" + (int) (Math.random() * 10000000);

        UpayOrder order = new UpayOrder();
        order.setClient_sn(clientSn);//商户订单号
        order.setTotal_amount("100");//交易总金额
        // order.setPayway("1");//支付方式--无需指定
        order.setDynamic_id(scanCode);//付款码内容
        order.setSubject("购物");//交易简介
        order.setOperator(trade.getCashier());//门店操作员
        order.setDescription("零售商品");//商品详情
        order.setReflect(trade.getLsNo());//反射参数
        order.setPayModel(UpayOrder.PayModel.NO_UI);//指定 SDK 启动模式为无界面模式

        final String requestNo = CommonUtil.newUuid();
        // 记录支付请求信息
        new SqbPayOrder(order, requestNo, "1", trade.getLsNo()).insert();//1-付款，2-退款，3-查询，4-撤单，5-预下单

        UpayTask.getInstance().pay(order, new UpayCallBack() {
            @Override
            public void onExecuteResult(UpayResult result) {
                dealWithResult(requestNo, result, callback);
            }
        });
    }

    /*
    https://api.shouqianba.com/upay/v2/refund
    {"terminal_sn":"100035370009271610","client_sn":"client8673787","refund_request_no":"7894259244086017",
     "operator":"508","refund_amount":"100","longitude":"4.9E-324","latitude":"4.9E-324",
     "device_id":"1F0070AA33AB6AE195D74BC1899F3EA1","reflect":"7894259244086017"}

     UpayResult
     {terminal_sn='null', terminal_key='null', sn='7895238408655453', client_sn='client8673787',
      trade_no='242019101522001493110554510275', status='SUCCESS', order_status='REFUNDED',
      payway='1', sub_payway='1', qr_code='null', payer_uid='2088002572693112',
      payer_login='red***@sina.com', total_amount='100', net_amount='0', subject='购物',
      finish_time='1571129661145', channel_finish_time='1571129660000', operator='508',
      description='零售商品', reflect='7894259244086017', refund_request_no='7894259244086017',
      result_code='REFUND_SUCCESS', error_code='', error_message=''}
     */

    /**
     * 按商户订单号退款
     *
     * @param clientSn 商户订单号
     * @param callback 退款结果回调
     */
    public static void refundByClientSn(String clientSn, final PayResultCallback callback) {
        refund("", clientSn, callback);
    }

    /**
     * 按收钱吧订单号退款
     *
     * @param sn       收钱吧订单号
     * @param callback 退款结果回调
     */
    public static void refundBySn(String sn, final PayResultCallback callback) {
        refund(sn, "", callback);
    }

    /**
     * 退款操作（sn和clientSn不能同时为空）
     *
     * @param sn       收钱吧订单号
     * @param clientSn 商户订单号
     * @param callback 退款结果回调
     */
    private static void refund(String sn, String clientSn, final PayResultCallback callback) {
        final String requestNo = CommonUtil.newUuid().substring(0, 30);

        UpayOrder order = new UpayOrder();
        //无UI(sn和client_sn不能同时为空)
        order.setSn(sn);//收钱吧订单号
        order.setClient_sn(clientSn);//商户订单号
        //商户退款所需序列号，用于唯一标识某次退款请求，以防止意外的重复退款。
        // 正常情况下，对同一笔订单进行多次退款请求时该字段不能重复；
        // 而当通信质量不佳，终端不确认退款请求是否成功，自动或手动发起的退款请求重试，则务必要保持序列号不变
        order.setRefund_request_no(requestNo);//退款序列号
        order.setOperator(ZgParams.getCurrentUser().getUserCode());//操作员
        order.setRefund_amount("100");//退款金额
        order.setReflect(requestNo);//反射参数
        order.setRefundModel(UpayOrder.RefundModel.CLIENT_SN);//指定退款模式为商户订单号退款
        order.setPayModel(UpayOrder.PayModel.NO_UI);//指定 SDK 启动模式为无界面模式

        // 记录退款请求信息
        new SqbPayOrder(order, requestNo, "2", "").insert();//1-付款，2-退款，3-查询，4-撤单，5-预下单

        UpayTask.getInstance().refund(order, new UpayCallBack() {
            @Override
            public void onExecuteResult(UpayResult result) {
                dealWithResult(requestNo, result, callback);
            }
        });
    }

    private static void query(String sn, String clientSn, final PayResultCallback callback) {
        UpayOrder order = new UpayOrder();
        order.setSn(sn);//收钱吧订单号
        order.setClient_sn(clientSn);//商户订单号

        final String requestNo = CommonUtil.newUuid();
        // 记录查询请求信息
        new SqbPayOrder(order, requestNo, "3", "").insert();//1-付款，2-退款，3-查询，4-撤单，5-预下单

        UpayTask.getInstance().query(order, new UpayCallBack() {
            @Override
            public void onExecuteResult(UpayResult result) {
                dealWithResult(requestNo, result, callback);
            }
        });
    }

    private static void dealWithResult(String requestNo, UpayResult result, final PayResultCallback callback) {
        // 记录查询结果信息
        new SqbPayResult(result, requestNo).insert();

        if (callback == null) {
            return;
        }
        // TODO: 2019/10/16 这里需要综合判断：status流水状态, order_status订单状态, result_code业务执行结果返回码
        if (result.getOrder_status().equals(UpayResult.ORDER_PAID)
                || result.getOrder_status().equals(UpayResult.ORDER_REFUNDED)
                || result.getOrder_status().equals(UpayResult.ORDER_PARTIAL_REFUNDED)
                || result.getOrder_status().equals(UpayResult.ORDER_CANCELED)) {
            callback.onResult(true, true, "");
        } else if (result.getOrder_status().equals(UpayResult.ORDER_PAY_CANCELED)) {
            callback.onResult(true, false, result.getError_message());
        } else {
            callback.onResult(false, true, "");
        }
    }
    /*
biz_response.result_code,状态分为：状态分为 SUCCESS、FAIL、INPROGRESS和 ERROR 四类，
SUCCESS: 本次业务执行成功
FAIL: 本次业务执行失败
INPROGRESS: 本次业务进行中
ERROR: 本次业务执行结果未知
具体到业务场景，分别有下列状态：
取值	                        含义                                         下一步动作
PAY_SUCCESS	                支付操作成功                                  银货两讫
PAY_FAIL	                支付操作失败并且已冲正                         重新进行一笔交易
PAY_IN_PROGRESS	            支付中                                       调用查询接口查询
PAY_FAIL_ERROR	            支付操作失败并且不确定第三方支付通道状态         联系客服
PAY_FAIL_IN_PROGRESS	    支付操作失败中并且不清楚状态	                 联系客服
CANCEL_SUCCESS	            撤单操作成功                                  --
CANCEL_ERROR	            撤单操作失败并且不确定第三方支付通道状态        联系客服
CANCEL_ABORT_ERROR	        撤单操作试图终止进行中的支付流程，但是失败，
                            不确定第三方支付通道的状态	                    联系客服
CANCEL_ABORT_SUCCESS	    撤单操作试图终止进行中的支付流程并且成功        --
CANCEL_IN_PROGRESS	        撤单进行中                                   调用查询接口进行查询
CANCEL_ABORT_IN_PROGRESS	撤单操作试图终止进行中的支付流程，
                            但是撤单状态不明确                            调用查询接口进行查询
REFUND_SUCCESS	            退款操作成功
REFUND_ERROR	            退款操作失败并且不确定第三方支付通状态	        联系客服
REFUND_FAIL	                退款失败
REFUND_IN_PROGRESS	        退款进行中
PRECREATE_SUCCESS	        预下单操作成功
PRECREATE_FAIL	            预下单操作失败
PRECREATE_FAIL_ERROR	    预下单状态失败并且不确定第三方支付通道状态	    联系客服
PRECREATE_FAIL_IN_PROGRESS	预下单状态失败并且不清楚状态	                联系客服
SUCCESS	                    操作成功,开发者根据返回的biz_response.data.order_status属性判断当前收钱吧订单的状态。
FAIL	                    操作失败（不会触发流程）
     */
    /*
订单状态列表
biz_response.data.order_status
取值	                含义
CREATED	            订单已创建/支付中
PAID	            订单支付成功
PAY_CANCELED	    支付失败并且已经成功充正
PAY_ERROR	        支付失败，不确定是否已经成功充正,请联系收钱吧客服确认是否支付成功
REFUNDED	        已成功全额退款
PARTIAL_REFUNDED	已成功部分退款
REFUND_ERROR	    退款失败并且不确定第三方支付通道的最终退款状态
CANCELED	        客户端发起的撤单已成功
CANCEL_ERROR	    客户端发起的撤单失败并且不确定第三方支付通道的最终状态
CANCEL_INPROGRESS	撤单进行中
INVALID_STATUS_CODE	无效的状态码
开发者根据返回的biz_response.data.order_status属性判断当前收钱吧订单的状态。
哪些状态是订单最终状态
    PAID
    PAY_CANCELED
    REFUNDED
    PARTIAL_REFUNDED
    CANCELED
     */
    /*
status流水状态列表
取值	            含义	                                        处理逻辑
SUCCESS	        业务执行确认成功（即收钱吧后台和消费者端均成功）	银货两讫（无论是交货还是退货）
FAIL_CANCELED	确认失败（即收钱吧后台和消费者端均失败）	        银货两讫，（不交货或是不退货）
其他          	错误	                                        小概率事件，失败但不确认消费者端状态
                                                            （即收钱吧后台强制认为是失败，但不确认消费者端是否同步失败）
                                                            （如果是收款，则不交货，但立即联系收钱吧客服，
                                                            （即算是消费者显示成功付款；
                                                            （如果是退货，则马上把货品回收，
                                                            （同时立即联系收钱吧客服，由收钱吧客服负责将钱款退回。
     */
}
