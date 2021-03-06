package com.virjar.xposedhooktool.tool.webview;

import android.os.Build;

import com.virjar.xposedhooktool.hotload.SingletonXC_MethodHook;
import com.virjar.xposedhooktool.tool.ReflectUtil;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by virjar on 2017/12/7.
 */
public class WebViewDebuggerController {
    private static Map<String, Boolean> enabledProcess = new HashMap<>();

    public synchronized static void enableDebug(final ClassLoader classLoader, final String packageName) {

        //hook需要放在webview创建之后，因为webview内部会持有context，在构造前context为null，导致空指针发生
        Class<?> webViewClass = XposedHelpers.findClass("android.webkit.WebView", classLoader);
        XposedBridge.hookAllConstructors(webViewClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (enabledProcess.containsKey(packageName)) {
                    return;
                }
                enabledProcess.put(packageName, true);
                try {
                    XposedBridge.log("开启浏览器调试...");
                    if (Build.VERSION.SDK_INT >= 19) {
                        //启用浏览器调试，可以在Android里面调试webview的代码
                        Class<?> webViewClass = XposedHelpers.findClass("android.webkit.WebView", classLoader);
                        XposedHelpers.callStaticMethod(webViewClass, "setWebContentsDebuggingEnabled", Boolean.TRUE);
                        ReflectUtil.findAndHookMethodOnlyByMethodName(webViewClass, "setWebContentsDebuggingEnabled", new SingletonXC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                param.args[0] = Boolean.TRUE;
                                //make sure webview debug disable in the future
                            }
                        });
                    }
                } catch (Exception e) {
                    XposedBridge.log("浏览器调试开启失败");
                    XposedBridge.log(e);
                }
            }
        });
    }
}
