package org.chen.statusbar;

/**
 * Created by chenzhaohua on 16/9/21.
 */
public class SystemStatusConstant {

    public static class Action {
        public static final String GPS_STATUS = "com.gezbox.status.GPS_STATUS";
        public static final String BATTERY_STATUS = "com.gezbox.status.BATTERY_STATUS";
        public static final String NET_STATUS = "com.gezbox.status.NET_STATUS";
        public static final String VOLUME_STATUS = "com.gezbox.status.VOLUME_STATUS";
    }

    public static class EXTRA {
        public static final String GPS_STATUS_EXTRA = "GPS_EXTRA";
        public static final String NET_STATUS_EXTRA = "NET_EXTRA";
        public static final String BATTERY_STATUS_EXTRA = "BATTERY_STATUS_EXTRA";
        public static final String VOLUME_STATUS_EXTRA = "VOLUME_STATUS_EXTRA";
    }


    public static final int NET_STATUS_OK = 0;                                //正常状态
    public static final int NET_STATUS_WEAK = 1;                              //信号不佳
    public static final int NET_STATUS_LOST = 2;                              //信号丢失
    public static final int NET_STATUS_CLOSED = 3;                            //网络关闭

    public static final int TASK_STATUS_OK = 0;                                //正常状态
    public static final int TASK_STATUS_EDGE = 1;                              //任务时间接近
    public static final int TASK_STATUS_OVER = 2;                              //任务超时
    public static final int TASK_STATUS_CONTINUE = 3;                          //时间变化,颜色不变

    public static final int GPS_STATUS_OK = 0;                                 //gps正常
    public static final int GPS_STATUS_WEAK = 1;                               //gps信号弱
    public static final int GPS_STATUS_LOST = 2;                               //gps信号丢失
    public static final int GPS_STATUS_CLOSED = 3;                             //gps关闭

    public static final int BATTERY_STATUS_OK = 1;                             //电池正常状态
    public static final int BATTERY_STATUS_WEAK = 2;                           //电池半电状态
    public static final int BATTERY_STATUS_LOST = 3;                           //电池缺电状态
    public static final int BATTERY_STATUS_CONTINUE = 4;                       //电量变化,颜色不变


    public static final int STREAM_MUSIC_STATUS_OK = 1;                        //媒体音量正常状态
    public static final int STREAM_MUSIC_STATUS_WEAK = 2;                      //媒体音量一半状态
    public static final int STREAM_MUSIC_STATUS_LOST = 3;                      //媒体音量较小状态

}
