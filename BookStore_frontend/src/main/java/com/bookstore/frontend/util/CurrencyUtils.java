package com.bookstore.frontend.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Tiện ích định dạng tiền tệ Việt Nam (VND).
 * Sử dụng thống nhất trên toàn bộ ứng dụng frontend.
 */
public final class CurrencyUtils {

    private static final Locale VI_LOCALE = new Locale("vi", "VN");
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(VI_LOCALE);

    static {
        VND_FORMAT.setMaximumFractionDigits(0);
        VND_FORMAT.setMinimumFractionDigits(0);
        VND_FORMAT.setGroupingUsed(true);
    }

    private CurrencyUtils() {
    }

    /**
     * Định dạng một số Double thành chuỗi VND (vd: "1.250.000 đ").
     */
    public static String formatVND(Double value) {
        if (value == null) {
            return "0 đ";
        }
        return VND_FORMAT.format(value) + " đ";
    }

    /**
     * Định dạng một số Long thành chuỗi VND (vd: "1.250.000 đ").
     */
    public static String formatVND(Long value) {
        if (value == null) {
            return "0 đ";
        }
        return VND_FORMAT.format(value) + " đ";
    }

    /**
     * Định dạng một BigDecimal thành chuỗi VND (vd: "1.250.000 đ").
     */
    public static String formatVND(BigDecimal value) {
        if (value == null) {
            return "0 đ";
        }
        return VND_FORMAT.format(value) + " đ";
    }

    /**
     * Định dạng có dấu trừ phía trước (vd: "- 50.000 đ").
     */
    public static String formatVNDWithSign(Double value) {
        if (value == null || value == 0) {
            return "0 đ";
        }
        if (value < 0) {
            return "- " + VND_FORMAT.format(Math.abs(value)) + " đ";
        }
        return VND_FORMAT.format(value) + " đ";
    }
}
