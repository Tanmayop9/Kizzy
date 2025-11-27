/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GetAppsUseCase.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.data.get_current_data.app

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.blankj.utilcode.util.AppUtils
import com.my.kizzy.data.rpc.CommonRpc
import com.my.kizzy.data.rpc.RpcImage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Objects
import java.util.SortedMap
import java.util.TreeMap
import javax.inject.Inject

class GetCurrentlyRunningApp @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appActivityDetector: AppActivityDetector
) {
    operator fun invoke(beginTime: Long = System.currentTimeMillis() - 10000): CommonRpc {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTimeMillis = System.currentTimeMillis()
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, beginTime, currentTimeMillis
        )
        if (queryUsageStats != null && queryUsageStats.size > 1) {
            val treeMap: SortedMap<Long, UsageStats> = TreeMap()
            for (usageStats in queryUsageStats) {
                treeMap[usageStats.lastTimeUsed] = usageStats
            }
            if (!(treeMap.isEmpty() || treeMap[treeMap.lastKey()]?.packageName == "com.my.kizzy" || treeMap[treeMap.lastKey()]?.packageName == "com.discord")) {
                val packageName = treeMap[treeMap.lastKey()]!!.packageName
                Objects.requireNonNull(packageName)
                val appName = AppUtils.getAppName(packageName)

                // Use enhanced activity detection if available for this app
                if (appActivityDetector.hasEnhancedDetection(packageName)) {
                    return appActivityDetector.getEnhancedActivity(packageName, appName)
                }

                return CommonRpc(
                    name = appName,
                    details = null,
                    state = null,
                    largeImage = RpcImage.ApplicationIcon(packageName, context),
                    packageName = packageName
                )
            }
        }
        return CommonRpc()
    }
}