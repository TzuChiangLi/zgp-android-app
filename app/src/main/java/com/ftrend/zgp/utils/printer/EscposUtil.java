package com.ftrend.zgp.utils.printer;


import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ESC码打印的工具类
 *
 * @author CrazyBody
 */
public class EscposUtil {

    public static enum ASCII_CONTROL_CODE {
        NUL(0), SOH(1), STX(2), ETX(3), EOT(4), ENQ(5), ACK(6), BEL(7), BS(8), HT(
                9), LF(10), VT(11), FF(12), CR(13), SO(14), SI(15), DLE(16), DC1(
                17), DC2(18), DC3(19), DC4(20), NAK(21), SYN(22), ETB(23), CAN(
                24), EM(25), SUB(26), ESC(27), FS(28), GS(29), RS(30), US(31);

        private final int value;

        private ASCII_CONTROL_CODE(int value) {
            this.value = value;
        }

        public byte getASCIIValue() {
            return (byte) this.value;
        }
    }

    public static byte[] convertEscposToBinary(String escpos) {
        int value = -1;

        StringReader r = new StringReader(escpos);
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.slashSlashComments(false);
        st.slashStarComments(false);
        st.whitespaceChars(0, 32);
        st.wordChars(33, 255);
        st.quoteChar(34);
        st.quoteChar(39);
        st.eolIsSignificant(true);

        ByteBuffer bb = ByteBuffer.allocate(2048);

        try {
            while (st.nextToken() != -1) {
                switch (st.ttype) {
                    case -3:
                        String s = st.sval;
                        value = -1;

                        if ((s.length() == 1) && (!Character.isDigit(s.charAt(0)))) {
                            byte[] bytes = s.getBytes();
                            value = bytes[0];
                        } else if ((s.length() > 2) && (s.substring(0, 2) == "0x")) {
                            value = Integer.parseInt(s.substring(2), 16);
                        } else if (isInteger(s)) {
                            value = Integer.parseInt(s);
                        } else if (s.contentEquals("NUL")) {
                            value = ASCII_CONTROL_CODE.NUL.getASCIIValue();
                        } else if (s.contentEquals("SOH")) {
                            value = ASCII_CONTROL_CODE.SOH.getASCIIValue();
                        } else if (s.contentEquals("STX")) {
                            value = ASCII_CONTROL_CODE.STX.getASCIIValue();
                        } else if (s.contentEquals("ETX")) {
                            value = ASCII_CONTROL_CODE.ETX.getASCIIValue();
                        } else if (s.contentEquals("EOT")) {
                            value = ASCII_CONTROL_CODE.EOT.getASCIIValue();
                        } else if (s.contentEquals("ENQ")) {
                            value = ASCII_CONTROL_CODE.ENQ.getASCIIValue();
                        } else if (s.contentEquals("ACK")) {
                            value = ASCII_CONTROL_CODE.ACK.getASCIIValue();
                        } else if (s.contentEquals("BEL")) {
                            value = ASCII_CONTROL_CODE.BEL.getASCIIValue();
                        } else if (s.contentEquals("BS")) {
                            value = ASCII_CONTROL_CODE.BS.getASCIIValue();
                        } else if (s.contentEquals("HT")) {
                            value = ASCII_CONTROL_CODE.HT.getASCIIValue();
                        } else if (s.contentEquals("LF")) {
                            value = ASCII_CONTROL_CODE.LF.getASCIIValue();
                        } else if (s.contentEquals("VT")) {
                            value = ASCII_CONTROL_CODE.VT.getASCIIValue();
                        } else if (s.contentEquals("FF")) {
                            value = ASCII_CONTROL_CODE.FF.getASCIIValue();
                        } else if (s.contentEquals("CR")) {
                            value = ASCII_CONTROL_CODE.CR.getASCIIValue();
                        } else if (s.contentEquals("SO")) {
                            value = ASCII_CONTROL_CODE.SO.getASCIIValue();
                        } else if (s.contentEquals("SI")) {
                            value = ASCII_CONTROL_CODE.SI.getASCIIValue();
                        } else if (s.contentEquals("DLE")) {
                            value = ASCII_CONTROL_CODE.DLE.getASCIIValue();
                        } else if (s.contentEquals("DC1")) {
                            value = ASCII_CONTROL_CODE.DC1.getASCIIValue();
                        } else if (s.contentEquals("DC2")) {
                            value = ASCII_CONTROL_CODE.DC2.getASCIIValue();
                        } else if (s.contentEquals("DC3")) {
                            value = ASCII_CONTROL_CODE.DC3.getASCIIValue();
                        } else if (s.contentEquals("DC4")) {
                            value = ASCII_CONTROL_CODE.DC4.getASCIIValue();
                        } else if (s.contentEquals("NAK")) {
                            value = ASCII_CONTROL_CODE.NAK.getASCIIValue();
                        } else if (s.contentEquals("SYN")) {
                            value = ASCII_CONTROL_CODE.SYN.getASCIIValue();
                        } else if (s.contentEquals("ETB")) {
                            value = ASCII_CONTROL_CODE.ETB.getASCIIValue();
                        } else if (s.contentEquals("CAN")) {
                            value = ASCII_CONTROL_CODE.CAN.getASCIIValue();
                        } else if (s.contentEquals("EM")) {
                            value = ASCII_CONTROL_CODE.EM.getASCIIValue();
                        } else if (s.contentEquals("SUB")) {
                            value = ASCII_CONTROL_CODE.SUB.getASCIIValue();
                        } else if (s.contentEquals("ESC")) {
                            value = ASCII_CONTROL_CODE.ESC.getASCIIValue();
                        } else if (s.contentEquals("FS")) {
                            value = ASCII_CONTROL_CODE.FS.getASCIIValue();
                        } else if (s.contentEquals("GS")) {
                            value = ASCII_CONTROL_CODE.GS.getASCIIValue();
                        } else if (s.contentEquals("RS")) {
                            value = ASCII_CONTROL_CODE.RS.getASCIIValue();
                        } else if (s.contentEquals("US")) {
                            value = ASCII_CONTROL_CODE.US.getASCIIValue();
                        }

                        if (value != -1) {
                            bb.put((byte) value);
                        }
                        break;
                    case 34:
                    case 39:
                        String str = st.sval;

                        for (int i = 0; i < str.length(); i++) {
                            byte b = str.getBytes()[i];
                            bb.put(b);
                        }
                }

            }
        } catch (NumberFormatException localNumberFormatException) {
        } catch (IOException e) {
        }

        byte[] result = null;
        int size = bb.position();
        if (size > 0) {
            result = new byte[size];
            bb.flip();
            bb.get(result);
        }

        return result;
    }

    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
        }

        return false;
    }


    // ------------------------常用指令封装-----------------------------
    public   static int iSplitCount=32;//58mm=32,80mm=48
    private static String ChinaRegEx = "[\u4e00-\u9fa5]";
    public static final byte ESC = 0x1B;// Escape
    public static final byte FS = 0x1C;// Text delimiter
    public static final byte GS = 0x1D;// Group separator
    public static final byte DLE = 0x10;// data link escape
    public static final byte EOT = 0x04;// End of transmission
    public static final byte ENQ = 0x05;// Enquiry character
    public static final byte SP = 0x20;// Spaces
    public static final byte HT = 0x09;// Horizontal list
    public static final byte LF = 0x0A;//Print and wrap (horizontal orientation)
    public static final byte CR = 0x0D;// Home key
    public static final byte FF = 0x0C;// Carriage control (print and return to the standard mode (in page mode))
    public static final byte CAN = 0x18;// Canceled (cancel print data in page mode)



    // ------------------------Align-----------------------------

    /**
     * 居左
     */
    public static byte[] alignLeft() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 0;
        return result;
    }

    /**
     * 居中对齐
     */
    public static byte[] alignCenter() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 1;
        return result;
    }

    /**
     * 居右
     */
    public static byte[] alignRight() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 97;
        result[2] = 2;
        return result;
    }

    // ------------------------underline-----------------------------
    //设置下划线1点
    public static byte[] underlineWithOneDotWidthOn() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 45;
        result[2] = 1;
        return result;
    }

    //设置下划线2点
    public static byte[] underlineWithTwoDotWidthOn() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 45;
        result[2] = 2;
        return result;
    }

    //取消下划线
    public static byte[] underlineOff() {
        byte[] result = new byte[3];
        result[0] = ESC;
        result[1] = 45;
        result[2] = 0;
        return result;
    }

}
