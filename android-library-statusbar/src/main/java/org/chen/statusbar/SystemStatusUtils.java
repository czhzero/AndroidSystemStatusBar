package org.chen.statusbar;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by chenzhaohua on 16/9/21.
 */
class SystemStatusUtils {





    /**
     * 获取网络类型
     * @param context
     * @return
     */
    public static String getNetworkType(Context context) {
        String strNetworkType = "";

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                strNetworkType = "WIFI";
            } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                String subTypeName = networkInfo.getSubtypeName();

                // TD-SCDMA   networkType is 17
                int networkType = networkInfo.getSubtype();
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:  //api<8 : replace by 11
                        strNetworkType = "2G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                    case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                    case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                        strNetworkType = "3G";
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                        strNetworkType = "4G";
                        break;
                    default:
                        //中国移动 联通 电信 三种3G制式
                        if (subTypeName.equalsIgnoreCase("TD-SCDMA")
                                || subTypeName.equalsIgnoreCase("WCDMA")
                                || subTypeName.equalsIgnoreCase("CDMA2000")) {
                            strNetworkType = "3G";
                        } else {
                            strNetworkType = subTypeName;
                        }
                        break;
                }

            }
        }

        return strNetworkType;
    }




    /**
     * 获取4G信号强度
     */
    public static int getLteLevel(String[] parts) {
        int level = SystemStatusConstant.NET_STATUS_OK;

        int rssiIconLevel;

        try {
            rssiIconLevel = Integer.parseInt(parts[8]);
        } catch (Exception e) {
            //避免信号强度存储方式不兼容报错
            return level;
        }

        if (rssiIconLevel > 63) {
            level = SystemStatusConstant.NET_STATUS_LOST;
        } else if (rssiIconLevel >= 8) {
            level = SystemStatusConstant.NET_STATUS_OK;
        } else if (rssiIconLevel > 0) {
            level = SystemStatusConstant.NET_STATUS_WEAK;
        }
        return level;
    }


    /**
     * 获取移动3G信号强度
     *
     * @param parts
     * @return
     */
    public static int getSdcdmaLevel(String[] parts) {
        int level = SystemStatusConstant.NET_STATUS_OK;

        int dbm;

        try {
            dbm = Integer.parseInt(parts[13]);
        } catch (Exception e) {
            //避免信号强度存储方式不兼容报错
            return level;
        }

        if (dbm > -25) {
            level = SystemStatusConstant.NET_STATUS_LOST;
        } else if (dbm >= -73) {
            level = SystemStatusConstant.NET_STATUS_OK;
        } else if (dbm >= -110) {
            level = SystemStatusConstant.NET_STATUS_WEAK;
        } else {
            level = SystemStatusConstant.NET_STATUS_LOST;
        }

        return level;
    }


    /**
     * 获取移动联通2G信号强度
     *
     * @param parts
     * @return
     */
    public static int getGsmLevel(String[] parts) {

        int level = SystemStatusConstant.NET_STATUS_OK;

        int asu;

        try {
            asu = Integer.parseInt(parts[1]);
        } catch (Exception e) {
            //避免信号强度存储方式不兼容报错
            return level;
        }

        if (asu <= 2 || asu == 99) {
            level = SystemStatusConstant.NET_STATUS_LOST;
        } else if (asu >= 8) {
            level = SystemStatusConstant.NET_STATUS_OK;
        } else if (asu >= 5) {
            level = SystemStatusConstant.NET_STATUS_WEAK;
        } else {
            level = SystemStatusConstant.NET_STATUS_LOST;
        }
        return level;

    }


    public static void sendBroadcast(Context context, String action, String extra, int value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(extra, value);
        intent.setPackage(context.getPackageName());
        if (context != null) {
            context.sendBroadcast(intent);
        }
    }


    /**
     * 检测是否打开gps
     *
     * @param context
     * @return
     */
    public static boolean isGPSOn(Context context) {
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
