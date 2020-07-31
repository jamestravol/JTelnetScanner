package jt.upwork.telnet.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author jamestravol
 */
public class Utils {

    public static final long MILLS_IN_24H = 86400000L;

    public static String getStackTrace(Throwable ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }


    public static String getFilename(long startTime, int shiftSetting) {

        long millsPassed = System.currentTimeMillis() - startTime;

        long stepSize = MILLS_IN_24H / shiftSetting;

        return getFilename(millsPassed / stepSize);
    }

    public static String getFilename(long shift) {
        final String dateString = LocalDate.now().format(DateTimeFormatter.ofPattern(Config.INSTANCE.getProperty("app.filename.date.format", "ddMMyyyy")));

        char ch = 'A';
        ch += shift;

        return dateString + "-" + ch + ".xls";

    }

}
