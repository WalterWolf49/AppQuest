package ch.TardisCoreTeam.Groessenmesser;

import java.text.DecimalFormat;

public class AppUtils {
    public AppUtils() {
    }

    public static String formatDouble(Double number){
        //#0.00 --> 123.4567 = 123.45 | 0.123456 = 0.12
        return  new DecimalFormat("#0.0").format(number);
    }
}
