package com.example.androidWebViewOfflineApplication

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class AppLockAccessibilityService : AccessibilityService() {

    private val blockedApps = setOf(
        "com.whatsapp",
        "com.instagram.android",
        "com.android.settings"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val packageName = event?.packageName?.toString() ?: return

        if (blockedApps.contains(packageName)) {

            val intent = Intent(this, LockScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}
}
