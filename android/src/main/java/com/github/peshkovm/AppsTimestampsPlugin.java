package com.github.peshkovm;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@NativePlugin
public class AppsTimestampsPlugin extends Plugin {

    @PluginMethod
    public void getAppsTimestamps(PluginCall call) {
        Set<Map<String, String>> runningAppsTimestamps = new HashSet<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean useGranted = isUseGranted();
            System.out.println(useGranted);
            if (useGranted) {
                runningAppsTimestamps = getRunningAppsTimestamps(getContext());
            } else {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        } else {
            runningAppsTimestamps = getRunningAppsTimestamps(getContext());
        }

        JSONArray resArray = new JSONArray(runningAppsTimestamps);

        JSObject ret = new JSObject();
        ret.put("value", resArray);
        call.success(ret);
    }

    private Set<Map<String, String>> getRunningAppsTimestamps(Context context) {
        Set<Map<String, String>> runningAppsTimestamps = new HashSet<>();

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> runningApplications = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : runningApplications) {
            //system apps! get out

            if (!isAppStopped(appInfo) && !isAppSystem(appInfo)) {
                Set<Map<String, String>> allAppsTimestamps = getAllAppsTimestamps();

                for (Map<String, String> appTimestamps : allAppsTimestamps) {
                    if (appTimestamps.get("packageName").equals(appInfo.packageName)) {
                        runningAppsTimestamps.add(appTimestamps);
                    }
                }
            }
        }

        System.out.println(runningAppsTimestamps);

        return runningAppsTimestamps;
    }

    private Set<Map<String, String>> getAllAppsTimestamps() {
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getContext().getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = 0;

        List<UsageStats> appsUsageStats = usageStatsManager.
                queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);

        Set<Map<String, String>> appsTimestampsSet = new HashSet<>();

        for (UsageStats appUsageStats : appsUsageStats) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                int firstTimeStamp = Math.toIntExact(appUsageStats.getFirstTimeStamp() / 1000);
                int lastTimeForegroundServiceUsed =
                        Math.toIntExact(appUsageStats.getLastTimeForegroundServiceUsed() / 1000);
                int lastTimeStamp = Math.toIntExact(appUsageStats.getLastTimeStamp() / 1000);
                int lastTimeUsed = Math.toIntExact(appUsageStats.getLastTimeUsed() / 1000);
                int lastTimeVisible = Math.toIntExact(appUsageStats.getLastTimeVisible() / 1000);
                int totalTimeForegroundServiceUsed =
                        Math.toIntExact(appUsageStats.getTotalTimeForegroundServiceUsed() / 1000);
                int totalTimeInForeground =
                        Math.toIntExact(appUsageStats.getTotalTimeInForeground() / 1000);
                int totalTimeVisible = Math.toIntExact(appUsageStats.getTotalTimeVisible() / 1000);

                Map<String, String> appTimestampsMap = new MyHashMap<>();

                appTimestampsMap.put("packageName", appUsageStats.getPackageName());
                appTimestampsMap.put("firstTimeStamp", String.valueOf(firstTimeStamp));
                appTimestampsMap.put("lastTimeForegroundServiceUsed",
                        String.valueOf(lastTimeForegroundServiceUsed));
                appTimestampsMap.put("lastTimeStamp", String.valueOf(lastTimeStamp));
                appTimestampsMap.put("lastTimeUsed", String.valueOf(lastTimeUsed));
                appTimestampsMap.put("lastTimeVisible", String.valueOf(lastTimeVisible));
                appTimestampsMap.put("totalTimeForegroundServiceUsed",
                        String.valueOf(totalTimeForegroundServiceUsed));
                appTimestampsMap.put("totalTimeInForeground", String.valueOf(totalTimeInForeground));
                appTimestampsMap.put("totalTimeVisible", String.valueOf(totalTimeVisible));

                if (appsTimestampsSet.contains(appTimestampsMap)) {
                    appsTimestampsSet.remove(appTimestampsMap);
                }

                appsTimestampsSet.add(appTimestampsMap);
            }
        }

        return appsTimestampsSet;
    }

    class MyHashMap<K extends String, V extends String> extends HashMap<K, V> {
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null)
                return false;
            if (getClass() != o.getClass())
                return false;
            HashMap<K, V> hashMap = (HashMap<K, V>) o;
            return Objects.equals(this.get("packageName"), hashMap.get("packageName"));
        }

        @Override
        public int hashCode() {
            return Objects.requireNonNull(this.get("packageName")).hashCode();
        }
    }

    private static boolean isAppStopped(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) != 0);
    }

    private static boolean isAppSystem(ApplicationInfo applicationInfo) {
        return ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private boolean isUseGranted() {
        Context appContext = getContext();
        AppOpsManager appOps = (AppOpsManager) appContext.getSystemService(Context.APP_OPS_SERVICE);
        int mode = -1;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), appContext.getPackageName());
        }
        boolean granted = mode == AppOpsManager.MODE_ALLOWED;
        return granted;
    }
}
