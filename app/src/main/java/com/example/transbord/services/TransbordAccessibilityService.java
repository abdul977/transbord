package com.example.transbord.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TransbordAccessibilityService extends AccessibilityService {
    private static final String TAG = "TransbordAccessibility";
    private static TransbordAccessibilityService instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // We don't need to handle events here as we'll be called directly
    }

    @Override
    public void onInterrupt() {
        // No need to implement
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = getServiceInfo();
        if (info == null) {
            info = new AccessibilityServiceInfo();
        }

        // Set event types to capture
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_FOCUSED |
                AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        // Set feedback type
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        // Set flags
        info.flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;

        // Set notification timeout
        info.notificationTimeout = 50;

        // Apply the updated service info
        setServiceInfo(info);

        // Set the instance
        instance = this;

        Log.d(TAG, "TransbordAccessibilityService connected");
        Toast.makeText(this, "Transbord Accessibility Service is now active", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    /**
     * Injects text into the currently focused text field
     * @param text The text to inject
     * @return true if successful, false otherwise
     */
    public boolean injectText(String text) {
        if (text == null || text.isEmpty()) {
            Log.e(TAG, "Text is null or empty");
            return false;
        }

        Log.d(TAG, "Attempting to inject text: " + text);

        // Get the root node
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            Log.e(TAG, "No active window found");
            return false;
        }

        // Find all editable fields
        List<AccessibilityNodeInfo> editableNodes = findEditableNodes(rootNode);
        Log.d(TAG, "Found " + editableNodes.size() + " editable nodes");

        // First try: Find the focused node
        AccessibilityNodeInfo focusedNode = findFocusedNode(editableNodes);

        if (focusedNode != null) {
            Log.d(TAG, "Found focused node");
            // Inject text into the focused node
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            boolean result = focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

            // If direct text setting failed, try paste method
            if (!result) {
                Log.d(TAG, "Direct text setting failed, trying clipboard");
                // Copy text to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Transbord Text", text);
                clipboard.setPrimaryClip(clip);

                // Paste from clipboard
                result = focusedNode.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }

            // Clean up
            focusedNode.recycle();

            if (result) {
                Log.d(TAG, "Successfully injected text into focused node");
                rootNode.recycle();
                return true;
            }
        }

        // Second try: Use the first editable node if no focused node or injection failed
        if (!editableNodes.isEmpty()) {
            Log.d(TAG, "Trying first editable node");
            AccessibilityNodeInfo firstEditableNode = editableNodes.get(0);

            // First focus the node
            firstEditableNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);

            // Inject text into the node
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            boolean result = firstEditableNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

            // If direct text setting failed, try paste method
            if (!result) {
                Log.d(TAG, "Direct text setting failed for first node, trying clipboard");
                // Copy text to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Transbord Text", text);
                clipboard.setPrimaryClip(clip);

                // Paste from clipboard
                result = firstEditableNode.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }

            // Clean up
            firstEditableNode.recycle();

            if (result) {
                Log.d(TAG, "Successfully injected text into first editable node");
                rootNode.recycle();
                return true;
            }
        }

        // Third try: Try to find any node that accepts text input
        Log.d(TAG, "Trying to find any text input field");
        AccessibilityNodeInfo editableNode = findEditableNodeByClassName(rootNode);
        if (editableNode != null) {
            // Focus the node
            editableNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);

            // Inject text
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            boolean result = editableNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);

            // Try clipboard if direct method fails
            if (!result) {
                // Copy text to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Transbord Text", text);
                clipboard.setPrimaryClip(clip);

                // Paste from clipboard
                result = editableNode.performAction(AccessibilityNodeInfo.ACTION_PASTE);
            }

            editableNode.recycle();

            if (result) {
                Log.d(TAG, "Successfully injected text into found editable node");
                rootNode.recycle();
                return true;
            }
        }

        // Clean up
        rootNode.recycle();
        Log.e(TAG, "Failed to inject text");
        return false;
    }

    /**
     * Find all editable nodes in the hierarchy
     */
    private List<AccessibilityNodeInfo> findEditableNodes(AccessibilityNodeInfo node) {
        List<AccessibilityNodeInfo> editableNodes = new ArrayList<>();

        if (node == null) {
            return editableNodes;
        }

        // Check if this node is editable
        if (node.isEditable()) {
            editableNodes.add(node);
        }

        // Check all children
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                editableNodes.addAll(findEditableNodes(child));
            }
        }

        return editableNodes;
    }

    /**
     * Find the focused node among editable nodes
     */
    private AccessibilityNodeInfo findFocusedNode(List<AccessibilityNodeInfo> nodes) {
        for (AccessibilityNodeInfo node : nodes) {
            if (node.isFocused()) {
                return node;
            }
        }
        return null;
    }

    /**
     * Find an editable node by class name
     */
    private AccessibilityNodeInfo findEditableNodeByClassName(AccessibilityNodeInfo node) {
        if (node == null) {
            return null;
        }

        // Check common text input class names
        String className = node.getClassName() != null ? node.getClassName().toString() : "";
        if ((className.contains("EditText") ||
             className.contains("TextInputLayout") ||
             className.contains("TextView") && node.isEditable()) ||
             className.contains("TextInput")) {
            return AccessibilityNodeInfo.obtain(node);
        }

        // Check all children recursively
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                AccessibilityNodeInfo result = findEditableNodeByClassName(child);
                if (result != null) {
                    child.recycle();
                    return result;
                }
                child.recycle();
            }
        }

        return null;
    }

    /**
     * Get the instance of the service
     */
    public static TransbordAccessibilityService getInstance() {
        return instance;
    }

    /**
     * Check if the service is running
     */
    public static boolean isRunning() {
        return instance != null;
    }
}
