package org.chen.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.Calendar;

/**
 * Created by chenzhaohua on 16/9/21.
 */
class SystemStatusViewManager {

    private final int UPDATE_MIN_INTERVAL = 10000;                      //网络状态最小刷新间隔

    private Context mContext;
    private BroadcastReceiver mReceiver;
    private Handler mHandler;
    private IntentFilter mFilter;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private AudioManager mAudioManager;
    private MobileSignalStrengthListener mMobileSignalStrengthListener;
    private SystemStatusView mSystemStatusView;

    /**
     * 信号强度状态更新线程
     */
    private Runnable mSignalStrengthChangeRunnable = new Runnable() {
        @Override
        public void run() {
            updateNetWorkStatus();
        }
    };



    public SystemStatusViewManager(Context context, SystemStatusView view) {
        mSystemStatusView = view;
        initData(context);
    }


    /**
     * 注册广播监听
     */
    public void registerStatusBarReceiver() {
        mContext.registerReceiver(mReceiver, mFilter);
        mMobileSignalStrengthListener = new MobileSignalStrengthListener();
        mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * 取消广播监听
     */
    public void unregisterStatusBarReceiver() {
        mContext.unregisterReceiver(mReceiver);
        mTelephonyManager.listen(mMobileSignalStrengthListener, PhoneStateListener.LISTEN_NONE);
    }



    private void initData(Context context) {

        mContext = context;

        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_TIME_TICK);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mFilter.addAction("android.media.VOLUME_CHANGED_ACTION");



        mHandler = new Handler();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                switch (action) {
                    //时间广播
                    case Intent.ACTION_TIME_TICK:
                        updateTaskStatus(SystemStatusConstant.TASK_STATUS_CONTINUE);
                        break;
                    //网络连接状态(网络切换,网络开关)
                    case ConnectivityManager.CONNECTIVITY_ACTION:
                        updateNetWorkStatus();
                        break;
                    //WiFi信号强度变化
                    case WifiManager.RSSI_CHANGED_ACTION:
                        mHandler.removeCallbacks(mSignalStrengthChangeRunnable);
                        mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
                        break;
                    //GPS连接状态(Gps开关)
                    case LocationManager.MODE_CHANGED_ACTION:
                    case LocationManager.PROVIDERS_CHANGED_ACTION:
                        updateGpsStatus();
                        break;
                    //电量变化
                    case Intent.ACTION_BATTERY_CHANGED:
                        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                        int percentage = (level * 100) / scale;
                        updateBatteryStatus(percentage);
                        break;
                    //音量变化
                    case "android.media.VOLUME_CHANGED_ACTION":
                        updateVolumeStatus();
                        break;
                    default:
                        break;
                }
            }
        };

        //初始化各个状态, 电量不需要刻意初始化
        updateTaskStatus(SystemStatusConstant.TASK_STATUS_OK);
        updateNetWorkStatus();
        updateGpsStatus();
        updateVolumeStatus();

    }


    /**
     * 刷新媒体音量
     */
    private void updateVolumeStatus() {

        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        double maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        double percentage = curVolume / maxVolume;

        //低音量警告通知
        if (percentage < 0.5) {
            SystemStatusUtils.sendBroadcast(mContext, SystemStatusConstant.Action.VOLUME_STATUS,
                    SystemStatusConstant.EXTRA.VOLUME_STATUS_EXTRA, curVolume);
        }

        mSystemStatusView.refreshVolumeView(curVolume, maxVolume);

    }


    /**
     * 刷新电量
     *
     * @param percentage
     */
    private void updateBatteryStatus(int percentage) {

        //低电量20,15,10,5的时候,发送警告广播
        if (percentage == 20 || percentage == 15
                || percentage == 10 || percentage == 5) {
            SystemStatusUtils.sendBroadcast(mContext, SystemStatusConstant.Action.BATTERY_STATUS,
                    SystemStatusConstant.EXTRA.BATTERY_STATUS_EXTRA, percentage);
        }

        mSystemStatusView.refreshBatteryView(percentage);

    }



    /**
     * 刷新时间任务状态
     * @param status
     */
    private void updateTaskStatus(int status) {
        Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));
        mSystemStatusView.refreshTimeView(time, status);
    }


    /**
     * 刷新Gps状态
     */
    private void updateGpsStatus() {

        int status;

        if (SystemStatusUtils.isGPSOn(mContext)) {
            status = SystemStatusConstant.GPS_STATUS_OK;
        } else {
            status = SystemStatusConstant.GPS_STATUS_CLOSED;
        }


        //发送无GPS警告广播
        if (status == SystemStatusConstant.GPS_STATUS_CLOSED) {
            SystemStatusUtils.sendBroadcast(mContext, SystemStatusConstant.Action.GPS_STATUS,
                    SystemStatusConstant.EXTRA.GPS_STATUS_EXTRA, SystemStatusConstant.GPS_STATUS_CLOSED);
        }


        mSystemStatusView.refreshGpsView(status);
    }


    /**
     * 刷新网络信号状态
     */
    private void updateNetWorkStatus() {

        String networkType = SystemStatusUtils.getNetworkType(mContext);

        int status;

        switch (networkType) {
            case "WIFI":
                status = getWifiLevel();
                break;
            case "2G":
            case "3G":
            case "4G":
                status = getMobileLevel();
                break;
            default:
                status = SystemStatusConstant.NET_STATUS_LOST;
                networkType = "无信号";
                break;
        }


        //没信号时，发送广播警告
        if (networkType.equals("无信号")) {
            SystemStatusUtils.sendBroadcast(mContext, SystemStatusConstant.Action.NET_STATUS,
                    SystemStatusConstant.EXTRA.NET_STATUS_EXTRA, status);
        }

        mSystemStatusView.refreshSignalView(networkType, status);

    }





    /**
     * 监听移动信号的强度变化
     */
    private class MobileSignalStrengthListener extends PhoneStateListener {

        private SignalStrength mSignalStrength;


        public SignalStrength getSignalStrength() {
            return mSignalStrength;
        }


        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            mSignalStrength = signalStrength;
            mHandler.removeCallbacks(mSignalStrengthChangeRunnable);
            //最小更新时间为500ms
            mHandler.postDelayed(mSignalStrengthChangeRunnable, UPDATE_MIN_INTERVAL);
        }

    }


    /**
     * 获取wifi连接的强度状态
     *
     * @return
     */
    private int getWifiLevel() {

        int rssi = mWifiManager.getConnectionInfo().getRssi();
        //WIFI信号最强
        if (rssi > -70) {
            return SystemStatusConstant.NET_STATUS_OK;
        } else if (rssi < -90 && rssi > -70) {
            //WIFI信号较弱
            return SystemStatusConstant.NET_STATUS_WEAK;
        } else {
            //WIFI信号微弱
            return SystemStatusConstant.NET_STATUS_LOST;
        }

    }

    /**
     * 获取蜂窝连接的强度状态
     *
     * @return
     */
    private int getMobileLevel() {

        int level = SystemStatusConstant.NET_STATUS_OK;

        if (mMobileSignalStrengthListener == null
                || mMobileSignalStrengthListener.getSignalStrength() == null) {
            return level;
        }

        String signalStrength = mMobileSignalStrengthListener.getSignalStrength().toString();
        String[] parts = signalStrength.split(" ");

        switch (mTelephonyManager.getNetworkType()) {
            //移动联通2G
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                level = SystemStatusUtils.getGsmLevel(parts);
                break;
            //电信2G
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                break;
            //4G网络
            case TelephonyManager.NETWORK_TYPE_LTE:
                level = SystemStatusUtils.getLteLevel(parts);
                break;
            //移动3G网络
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                level = SystemStatusUtils.getSdcdmaLevel(parts);
                break;
            default:
                level = SystemStatusConstant.NET_STATUS_OK;
                break;
        }

        return level;

    }



}
