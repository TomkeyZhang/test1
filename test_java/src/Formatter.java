import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 格式化类
 * 
 * @author zhangqitong
 * 
 */
public class Formatter {
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");;

    public static String newThingTime(long timeMilliseconds) {
        long nowMilliseconds = System.currentTimeMillis();
        if (getHourDifference(nowMilliseconds, timeMilliseconds) >= 24) {
            // 时间超过一天，显示具体时间：如2011-5-23 16:34
            return dateFormatter.format(new Date(timeMilliseconds));
        } else if (getHourDifference(nowMilliseconds, timeMilliseconds) >= 1
                && getHourDifference(nowMilliseconds, timeMilliseconds) < 24) {
            return getHourDifference(nowMilliseconds, timeMilliseconds) + "小时前";
        } else if (getMinuteDifference(nowMilliseconds, timeMilliseconds) >= 1
                && getMinuteDifference(nowMilliseconds, timeMilliseconds) < 60) {
            return getMinuteDifference(nowMilliseconds, timeMilliseconds)
                    + "分钟前";
        } else {
            return "刚刚";
        }
    }

    /**
     * 相差的小时数
     * 
     * @param nowMilliseconds 当前时间
     * @param timeMilliseconds 待显示时间
     * @return
     */
    private static int getHourDifference(long nowMilliseconds,
            long timeMilliseconds) {
        return getMinuteDifference(nowMilliseconds, timeMilliseconds) / 60;
    }

    /**
     * 相差的分钟数
     * 
     * @param nowMilliseconds 当前时间
     * @param timeMilliseconds 待显示时间
     * @return
     */
    private static int getMinuteDifference(long nowMilliseconds,
            long timeMilliseconds) {
        return (int) (nowMilliseconds - timeMilliseconds) / (1000 * 60);
    }

    public static void main(String[] args) {
        long time = System.currentTimeMillis() - 1000 * 90;
        System.out.println(newThingTime(time));
        System.out.println(dateFormatter.format(new Date(time)));
        System.out.println(getMinuteDifference(System.currentTimeMillis(), time));
        System.out.println(getHourDifference(System.currentTimeMillis(), time));
    }
}
