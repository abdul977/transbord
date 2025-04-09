package com.example.transbord.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import com.example.transbord.services.TransbordAccessibilityService;

import java.util.List;

public class AccessibilityUtil {
    
    /**
     * Check if the accessibility service is enabled
     * @param context The context
     * @return true if enabled, false otherwise
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null) {
            return false;
        }
        
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().contains(context.getPackageName() + "/.services.TransbordAccessibilityService")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Open accessibility settings
     * @param context The context
     */
    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
