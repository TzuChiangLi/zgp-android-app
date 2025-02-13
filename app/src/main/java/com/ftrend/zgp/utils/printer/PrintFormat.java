package com.ftrend.zgp.utils.printer;

import com.ftrend.zgp.model.HandoverRecord;
import com.ftrend.zgp.model.Trade;
import com.ftrend.zgp.utils.RtnHelper;
import com.ftrend.zgp.utils.TradeHelper;
import com.ftrend.zgp.utils.ZgParams;
import com.ftrend.zgp.utils.http.RestBodyMap;
import com.ftrend.zgp.view.TradeReportActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 打印数据格式化
 *
 * @author liziqiang@ftrend.cn
 */

public class PrintFormat {
    /**
     * 根据打印纸宽度定义字符总数
     */
    public static int iSplitCount = 32;//默认是58mm=32的,也有80mm=48
    /**
     * 前半部分空格数量
     */
    private static int startSpace = iSplitCount == 32 ? 19 : 24;
    /**
     * 居左
     */
    public static final int ALIGN_LEFT = 0;
    /**
     * 居中
     */
    public static final int ALIGN_CENTER = 1;
    /**
     * 居右
     */
    public static final int ALIGN_RIGHT = 2;
    /**
     * 加粗指令
     */
    public static final byte[] BOLD_ON = {0x1B, 0x45, 0x1};
    /**
     * 取消加粗
     */
    public static final byte[] BOLD_OFF = {0x1B, 0x45, 0x0};
    /**
     * 中文区域
     */
    private static String ChinaRegEx = "[\u4e00-\u9fa5]";

    /**
     * @return 生成销售流水打印数据
     */
    public static List<PrintData> printSale() {
        List<PrintData> printDataList = new ArrayList<>();
        PrintData printData;
        Trade trade = TradeHelper.getTrade();
        //分割线
        printDataList.add(newLine());
        //柜台名
        printData = new PrintData();
        printData.setBold(true);
        printData.setFontSize(36);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(ZgParams.getCurrentDep().getDepName());
        printDataList.add(printData);
        //流水号
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("流水号:",
                trade.getLsNo().length() > 8 ? trade.getLsNo() : String.format("%s%s", new SimpleDateFormat("yyyyMMdd").format(trade.getTradeTime()), trade.getLsNo()), 32));
        printDataList.add(printData);
        //交易时间
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("交易时间:", new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(trade.getTradeTime()), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //商品明细标题
        //商品        数量    小计
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeSaleTitle("商品", "数量", "小计", 32));
//        printData.setPrintData("商品--------------数量------小计");
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //商品明细列表
        //商品名      数量     小计
        //健力宝      ×10   100.00
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setTypeList(true);
        printData.setProdList(TradeHelper.getProdList());
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //支付方式
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("支付方式：", TradeHelper.convertAppPayType(TradeHelper.getPay().getAppPayType(), trade.getDepCode()), 32));
        printDataList.add(printData);
        //商品原价
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("原价总计：", String.format("￥%.2f", TradeHelper.getProdTotal()), 32));
        printDataList.add(printData);
        //优惠总计
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("优惠总计：", String.format("￥-%.2f", trade.getDscTotal()), 32));
        printDataList.add(printData);
        //合计金额
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("合计金额：", String.format("￥%.2f", trade.getTotal()), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        return printDataList;
    }

    /**
     * @return 不按单退货生成退货流水打印数据
     */
    public static List<PrintData> printRtn() {
        List<PrintData> printDataList = new ArrayList<>();
        PrintData printData;
        Trade trade = RtnHelper.getRtnTrade();
        //分割线
        printDataList.add(newLine());
        //柜台名
        printData = new PrintData();
        printData.setBold(true);
        printData.setFontSize(36);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(ZgParams.getCurrentDep().getDepName());
        printDataList.add(printData);
        //流水号
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("流水号:",
                trade.getLsNo().length() > 8 ? trade.getLsNo() : String.format("%s%s", new SimpleDateFormat("yyyyMMdd").format(trade.getTradeTime()), trade.getLsNo()), 32));
        printDataList.add(printData);
        //交易时间
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("交易时间:", new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(trade.getTradeTime()), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //商品明细标题
        //商品        数量    小计
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeSaleTitle("商品", "数量", "小计", 32));
//        printData.setPrintData("商品--------------数量------小计");
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //商品明细列表
        //商品名      数量     小计
        //健力宝      ×10   100.00
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setTypeList(true);
        printData.setProdList(RtnHelper.getRtnProdList());
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //支付方式
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("支付方式：", TradeHelper.convertAppPayType(RtnHelper.getRtnPay().getAppPayType(), trade.getDepCode()), 32));
        printDataList.add(printData);
        //商品原价
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("原价总计：", String.format("￥%.2f", RtnHelper.getRtnTradePrice()), 32));
        printDataList.add(printData);
        //优惠总计
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("优惠总计：", String.format("￥-%.2f", trade.getDscTotal()), 32));
        printDataList.add(printData);
        //合计金额
        printData = new PrintData();
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("合计金额：", String.format("￥%.2f", trade.getTotal()), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        return printDataList;
    }

    /**
     * @param begin    开始日期
     * @param end      截止日期
     * @param dataList 明细列表
     * @param payList  支付明细
     * @return 打印数据列表
     */
    public static List<PrintData> printTradeReport(Date begin, Date end, List<RestBodyMap> dataList, List<TradeReportActivity.ReportData> payList) {
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        List<PrintData> printDataList = new ArrayList<>();
        PrintData printData;
        //分割线
        printDataList.add(newLine());
        //标题
        printData = new PrintData();
        printData.setBold(true);
        printData.setFontSize(32);
        printData.setAlign(ALIGN_CENTER);
        printData.setPrintData("交易统计报表");
        printDataList.add(printData);
        //柜台名
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(String.format("专柜：%s", ZgParams.getCurrentDep().getDepName()));
        printDataList.add(printData);
        //开始日期
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("开始日期:", new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(begin), 32));
        printDataList.add(printData);
        //截止日期
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("截止日期:", new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(end), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //交易统计标题
        //项目       金额      次数
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("项目", "金额", "次数", 32));
        printDataList.add(printData);
        //交易统计
        printData = new PrintData();
        printData.setTradeList(true);
        printData.setDataList(dataList);
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //支付明细统计
        if (payList != null) {
            printData = new PrintData();
            printData.setPayList(true);
            printData.setPayList(payList);
            printDataList.add(printData);
            //分割线
            printDataList.add(newLine());
        }
        //打印时间
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("打印时间:", new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(new Date()), 32));
        printDataList.add(printData);
        return printDataList;
    }

    /**
     * @param recordList 交班报表数据
     * @return 打印数据列表
     */
    public static List<PrintData> printHandoverTrade(List<HandoverRecord> recordList) {
        HandoverRecord data = recordList.get(0);
        List<PrintData> printDataList = new ArrayList<>();
        PrintData printData;
        //分割线
        printDataList.add(newLine());
        //标题
        printData = new PrintData();
        printData.setBold(true);
        printData.setFontSize(32);
        printData.setAlign(ALIGN_CENTER);
        printData.setPrintData("交班报表");
        printDataList.add(printData);
        //柜台名
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(String.format("收款员：%s", data.getCashierName()));
        printDataList.add(printData);
        //柜台名
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(String.format("专柜：%s", ZgParams.getCurrentDep().getDepName()));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //交易统计标题
        //项目       金额      次数
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("项目", "金额", "次数", 32));
        printDataList.add(printData);
        //收银
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("收银", String.format("%.2f", data.getSaleTotal()),
                String.format("%d", data.getSaleCount()), 32));
        printDataList.add(printData);
        //退货
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("退货", String.format("%.2f", data.getRtnTotal()),
                String.format("%d", data.getRtnCount()), 32));
        printDataList.add(printData);
        //交易合计
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("交易合计", String.format("%.2f", data.getTotal()),
                String.format("%d", data.getCount()), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //现金
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("现金", String.format("%.2f", data.getMoneyTotal()),
                String.format("%d", data.getMoneyCount()), 32));
        printDataList.add(printData);
        //收钱吧
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("收钱吧", String.format("%.2f", data.getSqbTotal()),
                String.format("%d", data.getSqbCount()), 32));
        printDataList.add(printData);
        //储值卡
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("交易统计", String.format("%.2f", data.getCardTotal()),
                String.format("%d", data.getCardCount()), 32));
        printDataList.add(printData);
        //支付方式合计
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setPrintData(mergeReportTitle("支付方式合计", String.format("%.2f", data.getPayTotal()),
                String.format("%d", data.getPayCount()), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());
        //打印时间
        printData = new PrintData();
        printData.setInitStyle(true);
        printData.setAlign(ALIGN_LEFT);
        printData.setPrintData(mergeSaleString("打印时间:", new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss").format(new Date()), 32));
        printDataList.add(printData);
        //分割线
        printDataList.add(newLine());

        return printDataList;
    }


    /**
     * @return 分割线
     */
    private static PrintData newLine() {
        //分割线
        PrintData line = new PrintData();
        String lineStr = "";
        for (int i = 0; i < iSplitCount; i++) {
            lineStr += "-";
        }
        line.setAlign(ALIGN_CENTER);
        line.setBold(false);
        line.setInitStyle(true);
        line.setPrintData(lineStr);
        return line;
    }

    /**
     * 标题格式化
     *
     * @param start  左标题
     * @param mid    中间标题
     * @param end    右标题
     * @param length 长度
     * @return 文本
     */
    public static String mergeReportTitle(String start, String mid, String end, int length) {
        String resultStr = "";
        int startLength = 0;
        int midLength = 0;
        int endLength = 0;
        //TODO 24这个值不准确，需要拿到80mm机器实际测试
        startSpace = iSplitCount == 32 ? 15 : 20;
        //int chineseCharNum = 0;
        // 计算开始的字符的总长度和中文字符串的数量
        for (int i = 0; i < start.length(); i++) {
            String temp = start.substring(i, i + 1);
            //Log.i("","---->Start:"+isChineseChar(temp)+":"+temp);
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                startLength += 2;
            } else {
                startLength += 1;
            }
        }

        for (int i = 0; i < mid.length(); i++) {
            String temp = mid.substring(i, i + 1);
            //Log.i("","---->Start:"+isChineseChar(temp)+":"+temp);
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                midLength += 2;
            } else {
                midLength += 1;
            }
        }

        // 计算结束的字符串的长度和总长度
        for (int i = 0; i < end.length(); i++) {
            String temp = end.substring(i, i + 1);
            //Log.i("","---->End:"+isChineseChar(temp));
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                endLength += 2;
            } else {
                endLength += 1;
            }
        }
        String space = "";
        for (int i = 0; i < startSpace - startLength; i++) {
            space += " ";
        }
        resultStr = start + space + mid;
        space = "";
        for (int i = 0; i < length - (startSpace + midLength + endLength); i++) {
            space += " ";
        }
        resultStr = resultStr + space + end;
        return resultStr;
    }


    /**
     * 标题格式化
     *
     * @param start  左标题
     * @param mid    中间标题
     * @param end    右标题
     * @param length 长度
     * @return 文本
     */
    private static String mergeSaleTitle(String start, String mid, String end, int length) {
        String resultStr = "";
        int startLength = 0;
        int midLength = 0;
        int endLength = 0;
        //TODO 24这个值不准确，需要拿到80mm机器实际测试
        startSpace = iSplitCount == 32 ? 19 : 24;
        //int chineseCharNum = 0;
        // 计算开始的字符的总长度和中文字符串的数量
        for (int i = 0; i < start.length(); i++) {
            String temp = start.substring(i, i + 1);
            //Log.i("","---->Start:"+isChineseChar(temp)+":"+temp);
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                startLength += 2;
            } else {
                startLength += 1;
            }
        }

        for (int i = 0; i < mid.length(); i++) {
            String temp = mid.substring(i, i + 1);
            //Log.i("","---->Start:"+isChineseChar(temp)+":"+temp);
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                midLength += 2;
            } else {
                midLength += 1;
            }
        }

        // 计算结束的字符串的长度和总长度
        for (int i = 0; i < end.length(); i++) {
            String temp = end.substring(i, i + 1);
            //Log.i("","---->End:"+isChineseChar(temp));
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                endLength += 2;
            } else {
                endLength += 1;
            }
        }
        String space = "";
        for (int i = 0; i < startSpace - startLength; i++) {
            space += " ";
        }
        resultStr = start + space + mid;
        space = "";
        for (int i = 0; i < length - (startSpace + midLength + endLength); i++) {
            space += " ";
        }
        resultStr = resultStr + space + end;
        return resultStr;
    }

    /**
     * 拼接三个字符串
     * 58mm标题初始化样式下的标准
     * 商品--------------数量------小计
     *
     * @param start  首字符串
     * @param mid    中字符串
     * @param end    尾字符串
     * @param length 字符串总长度(58mm下初始化为32)
     * @return
     */
    public static String[] mergeSaleString(String start, String mid, String end, int length) {
        String space = "";
        String firstLine = "";
        String secondLine = "";
        //直接换行，简单粗暴
        firstLine = start + "\n";
        //        计算字符数换行
//        if (startLength <= length) {
//            for (int i = 0; i < length - startLength - 1; i++) {
//                space += " ";
//            }
//            firstLine = start + space;
//        } else {
//            firstLine = start+"\n";
//        }
        for (int i = 0; i < startSpace; i++) {
            space += " ";
        }
        secondLine = space + mid;
        while (length - secondLine.length() - end.length() > 0) {
            secondLine += " ";
        }
        secondLine = secondLine + end;
        return new String[]{firstLine, secondLine};
    }


    /**
     * 拼接两个字符串
     *
     * @param start  开始的头字符串
     * @param end    结尾的尾字符串
     * @param length 字符串的总长度
     * @return 拼接之后的字符串
     */
    public static String mergeSaleString(String start, String end, int length) {
        String rtnStr = "";
        int startLength = 0;
        int endLength = 0;
        //int chineseCharNum = 0;
        // 计算开始的字符的总长度和中文字符串的数量
        for (int i = 0; i < start.length(); i++) {
            String temp = start.substring(i, i + 1);
            //Log.i("","---->Start:"+isChineseChar(temp)+":"+temp);
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                startLength += 2;
            } else {
                startLength += 1;
            }
        }

        // 计算结束的字符串的长度和总长度
        for (int i = 0; i < end.length(); i++) {
            String temp = end.substring(i, i + 1);
            //Log.i("","---->End:"+isChineseChar(temp));
            if (isChineseChar(temp)) {
                //chineseCharNum += 1;
                endLength += 2;
            } else {
                endLength += 1;
            }
        }

        String space = "";
        if ((endLength + startLength) <= length) {
            int loopNum = length - endLength - startLength;
            for (int i = 0; i < loopNum; i++) {
                space = space + " ";
            }
            rtnStr = start + space + end;
        } else {
            String temp = start + end;
//			rtnStr = temp.substring(0,length - 1 - chineseCharNum);
            for (int i = temp.length(); i > 0; i--) {
                String sub = temp.substring(0, i);
                int newLeng = 0;
                for (int j = 0; j < sub.length(); j++) {
                    String in = sub.substring(j, j + 1);
                    //Log.i("","---->Start:"+isChineseChar(temp)+":"+temp);
                    if (isChineseChar(in)) {
                        newLeng += 2;
                    } else {
                        newLeng += 1;
                    }
                }

                if (newLeng <= length) {
                    int loop = length - newLeng;
                    for (int j = 0; j < loop; j++) {
                        sub += " ";
                    }
                    rtnStr = sub;
                    break;
                }
            }
        }

        return rtnStr;
    }


    public static boolean isChineseChar(String s) {
        boolean rtn = false;
        Pattern p = Pattern.compile(ChinaRegEx);
        Matcher m = p.matcher(s);

        if (!m.find()) {
            if (s.getBytes().length == s.length()) {
                rtn = false;
            } else {
                rtn = true;
            }
        } else {
            rtn = true;
        }
        return rtn;
    }

    public static int getiSplitCount() {
        return iSplitCount;
    }

    public static void setiSplitCount(int iSplitCount) {
        PrintFormat.iSplitCount = iSplitCount;
    }
}
