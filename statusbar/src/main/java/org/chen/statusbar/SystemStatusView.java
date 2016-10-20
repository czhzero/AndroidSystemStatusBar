package org.chen.statusbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Created by chenzhaohua on 16/9/21.
 */
public class SystemStatusView extends RelativeLayout {


    private TextView tv_system_net;                                      //网络标示
    private TextView tv_system_time;                                     //时间标示
    private ImageView iv_system_location;                                //定位标示
    private TextView tv_system_battery;                                  //电量标示
    private ImageView iv_system_vol;                                     //音量标示
    private SystemStatusViewManager mSystemStatusManager;                //系统状态管理
    private Context mContext;


    public SystemStatusView(Context context) {
        this(context, null);
    }

    public SystemStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SystemStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }


    /**
     * 注册广播监听
     */
    public void registerStatusBarReceiver() {
        mSystemStatusManager.registerStatusBarReceiver();
    }

    /**
     * 取消广播监听
     */
    public void unregisterStatusBarReceiver() {
        mSystemStatusManager.unregisterStatusBarReceiver();
    }


    private void initView(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.system_status_view, this);
        tv_system_net = (TextView) findViewById(R.id.tv_system_net);
        tv_system_time = (TextView) findViewById(R.id.tv_system_time);
        iv_system_location = (ImageView) findViewById(R.id.iv_system_location);
        tv_system_battery = (TextView) findViewById(R.id.tv_system_battery);
        iv_system_vol = (ImageView) findViewById(R.id.iv_system_vol);
        mSystemStatusManager = new SystemStatusViewManager(context, this);
    }






    /**
     * 刷新音量布局
     */
    void refreshVolumeView(int curVolume, double maxVolume) {

        int status;

        double percentage = curVolume / maxVolume;

        //计算音量显示状态
        if (percentage > 0.8) {
            status = SystemStatusConstant.STREAM_MUSIC_STATUS_OK;
        } else if (percentage <= 0.8 && percentage > 0.5) {
            status = SystemStatusConstant.STREAM_MUSIC_STATUS_WEAK;
        } else {
            status = SystemStatusConstant.STREAM_MUSIC_STATUS_LOST;
        }


        Object tag = iv_system_vol.getTag();
        int preStatus = Integer.parseInt(tag == null ? "1" : tag.toString());

        //状态不变,直接返回
        if (status == preStatus) {
            return;
        }

        int srcDrawableId;
        int backgroudDrawableId;

        switch (status) {
            case SystemStatusConstant.STREAM_MUSIC_STATUS_OK:
                srcDrawableId = R.drawable.statusbar_vol_ok;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
                break;
            case SystemStatusConstant.STREAM_MUSIC_STATUS_WEAK:
                srcDrawableId = R.drawable.statusbar_vol_weak;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
                break;
            case SystemStatusConstant.STREAM_MUSIC_STATUS_LOST:
                srcDrawableId = R.drawable.statusbar_vol_lost;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
            default:
                srcDrawableId = R.drawable.statusbar_vol_lost;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), srcDrawableId);
        iv_system_vol.setImageBitmap(bitmap);
        iv_system_vol.setBackgroundResource(backgroudDrawableId);
        iv_system_vol.setTag(status);
    }


    /**
     * 刷新电量布局
     * @param percentage
     */
    void refreshBatteryView(int percentage) {


        String batteryInfo = percentage + "%";

        String currentBatteryInfo = tv_system_battery.getText().toString();

        if(batteryInfo.equals(currentBatteryInfo)) {
            return;
        }

        int currentPercentage = Integer.parseInt(currentBatteryInfo.substring(0,
                currentBatteryInfo.length() - 1));

        int status;

        //高电量
        if (percentage > 50) {
            if (currentPercentage > 50) {
                status = SystemStatusConstant.BATTERY_STATUS_CONTINUE;
            } else {
                status = SystemStatusConstant.BATTERY_STATUS_OK;
            }
        } else if (percentage > 20) {
            //半电量
            if (currentPercentage > 20) {
                status = SystemStatusConstant.BATTERY_STATUS_CONTINUE;
            } else {
                status = SystemStatusConstant.BATTERY_STATUS_WEAK;
            }
        } else {
            //弱电量
            if (currentPercentage <= 20) {
                status = SystemStatusConstant.BATTERY_STATUS_CONTINUE;
            } else {
                status = SystemStatusConstant.BATTERY_STATUS_LOST;
            }
        }


        int textColorId;
        int backgroudDrawableId;

        //只刷新电量,颜色不变
        if (status == SystemStatusConstant.BATTERY_STATUS_CONTINUE) {
            tv_system_battery.setText(batteryInfo);
            return;
        }

        switch (status) {
            //正常
            case SystemStatusConstant.BATTERY_STATUS_OK:
                textColorId = R.color.statusbar_text_green;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
                break;
            //警告
            case SystemStatusConstant.BATTERY_STATUS_WEAK:
                textColorId = R.color.statusbar_text_yellow;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
                break;
            //低电量
            case SystemStatusConstant.BATTERY_STATUS_LOST:
                textColorId = R.color.statusbar_text_red;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
            default:
                textColorId = R.color.statusbar_text_red;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
        }

        tv_system_battery.setText(batteryInfo);
        tv_system_battery.setTextColor(mContext.getResources().getColor(textColorId));
        tv_system_battery.setBackgroundResource(backgroudDrawableId);

    }


    /**
     * 刷新时间布局
     */
    void refreshTimeView(String time, int status) {

        int textColorId;
        int backgroudDrawableId;

        //只刷新时间数值,颜色不变
        if (status == SystemStatusConstant.TASK_STATUS_CONTINUE) {
            tv_system_time.setText(time);
            return;
        }

        switch (status) {
            //正常
            case SystemStatusConstant.TASK_STATUS_OK:
                textColorId = R.color.statusbar_text_green;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
                break;
            //警告
            case SystemStatusConstant.TASK_STATUS_EDGE:
                textColorId = R.color.statusbar_text_yellow;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
                break;
            //超时
            case SystemStatusConstant.TASK_STATUS_OVER:
                textColorId = R.color.statusbar_text_red;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
            default:
                textColorId = R.color.statusbar_text_red;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
        }

        tv_system_time.setText(time);
        tv_system_time.setTextColor(mContext.getResources().getColor(textColorId));
        tv_system_time.setBackgroundResource(backgroudDrawableId);

    }


    /**
     * 根据Gps信号状态，刷新Gps布局
     */
    void refreshGpsView(int status) {

        int srcDrawableId;
        int backgroudDrawableId;

        switch (status) {
            case SystemStatusConstant.GPS_STATUS_OK:
                srcDrawableId = R.drawable.statusbar_gps_ok;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
                break;
            case SystemStatusConstant.GPS_STATUS_WEAK:
                srcDrawableId = R.drawable.statusbar_gps_weak;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
                break;
            case SystemStatusConstant.GPS_STATUS_LOST:
            case SystemStatusConstant.GPS_STATUS_CLOSED:
                srcDrawableId = R.drawable.statusbar_gps_lost;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
            default:
                srcDrawableId = R.drawable.statusbar_gps_lost;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), srcDrawableId);
        iv_system_location.setImageBitmap(bitmap);
        iv_system_location.setBackgroundResource(backgroudDrawableId);

    }


    /**
     * 根据网络类型和网络状态，刷新网络布局
     */
    void refreshSignalView(String networkType, int status) {

        int textColorId;
        int backgroudDrawableId;

        //网络状态不改变时,不做任何界面刷新处理
        if (tv_system_net.getTag() != null
                && tv_system_net.getTag().toString().equals(networkType + status)) {
            return;
        }

        switch (status) {
            //正常绿色
            case SystemStatusConstant.NET_STATUS_OK:
                textColorId = R.color.statusbar_text_green;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_green;
                break;
            //微弱黄色
            case SystemStatusConstant.NET_STATUS_WEAK:
                textColorId = R.color.statusbar_text_yellow;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_yellow;
                break;
            //丢失红色
            case SystemStatusConstant.NET_STATUS_LOST:
            case SystemStatusConstant.NET_STATUS_CLOSED:
                textColorId = R.color.statusbar_text_red;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
            //默认红色
            default:
                textColorId = R.color.statusbar_text_red;
                backgroudDrawableId = R.drawable.statusbar_shape_bg_red;
                break;
        }

        tv_system_net.setText(networkType);
        tv_system_net.setTextColor(mContext.getResources().getColor(textColorId));
        tv_system_net.setBackgroundResource(backgroudDrawableId);
        tv_system_net.setTag(networkType + status);

    }


}
