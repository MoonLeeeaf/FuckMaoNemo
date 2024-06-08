package io.github.moonleeeaf.fuckmaonemo;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Hook implements IXposedHookLoadPackage {
    private static boolean isHooked = false;
    private XSharedPreferences xsp;
    private ClassLoader classLoader;
    private int nohengheng;
    private int aaaa;
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        if ("com.codemao.nemo".equals(param.packageName)) {
            XposedBridge.log("[FuckMaoNemo] 开始注入...");
            // 感谢 安宁 提供取加固程序的 ClassLoader 的代码
            XposedBridge.hookAllMethods(
                XposedHelpers.findClass("android.app.ActivityThread", param.classLoader),
                "performLaunchActivity",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam mParam) throws Throwable {
                        super.afterHookedMethod(mParam);
                        Object initApp = XposedHelpers.getObjectField(mParam.thisObject, "mInitialApplication");
                        classLoader = initApp.getClass().getClassLoader();
                        hook(param);
                    }
                }
            );
        }
    }
    
    public static Method getMethod(Class clazz, String name, Class<?>... args) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(name, args);
    }
   
    public Application getApplication() throws ClassNotFoundException {
        return (Application) XposedHelpers.callStaticMethod(Class.forName("android.app.ActivityThread"), "currentApplication");
    }
    
    public void hook(XC_LoadPackage.LoadPackageParam param) throws Exception {
        if (isHooked) return;
        else isHooked = true;
        
        nohengheng = 0;
        aaaa = 0;
        
        xsp = new XSharedPreferences("io.github.moonleeeaf.fuckmaonemo", "config");
        
        XposedBridge.log("[FuckMaoNemo] 注入中...");
        
        // 拦截40x码
        load("fuck_40x", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_拦截40x码");
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.retrofit.response.CommonSubcriber", classLoader),
                    "onNext",
                    XposedHelpers.findClass("retrofit2.Response", classLoader)
                ),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam mP) throws Throwable {
                        Object res = mP.args[0];
                        int code = (int) XposedHelpers.callMethod(res, "code");
                        if(code >= 400 && code <500) {
                            Object rawRes = XposedHelpers.getObjectField(res, "rawResponse");
                            XposedHelpers.setIntField(rawRes, "code", 200);
                            XposedBridge.log("[FuckMaoNemo] 拦截响应 " + code + " 码");
                            String t = "响应码 " + code;
                            // Toast.makeText(getApplication(), "[FuckMaoNemo] " + t, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            );
        });
        
        // 绕过防沉迷
        load("fuck_fcm", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_绕过防沉迷");
            methodToVoid(getMethod(
                XposedHelpers.findClass("com.codemao.nemo.activity.WorkDetailActivity", classLoader),
                "checkAntiAddictionState",
                null
            ));
        });
        
        // 强制显示再创作按钮
        load("force_show_rework", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_强制显示再创作按钮");
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.creativecenter.utils.bcm.bean.CreativeWorkDetailInfo", classLoader),
                    "isFork_enable",
                    null
                ),
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                        return true;
                    }
                }
            );
        });
        
        // 不追踪
        load("no_records", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_不追踪");
            methodToVoid(getMethod(
                XposedHelpers.findClass("cn.codemao.android.stat.CodeMaoStat", classLoader),
                "recordEvent",
                String.class,
                Map.class
            ));
        });
        
        // 反防抓包
        load("fuck_no_proxy", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_反防抓包");
            // TODO：其实可以从 OkHttp 底层去Hook的
            methodToVoid(
                getMethod(
                    XposedHelpers.findClass("okhttp3.OkHttpClient$Builder", classLoader),
                    "proxy",
                    Proxy.class
                )
            );
        });
        
        XposedBridge.log("[FuckMaoNemo] 执行完毕");
        
        Toast.makeText(getApplication(), "[FuckMaoNemo] 加载成功 (≧▽≦)\n" + nohengheng + " 个功能加载成功, " + aaaa + " 个失败", Toast.LENGTH_LONG).show();
    }
    
    public interface Callback {
        public void onCallback() throws Exception;
    }
    
    public void methodToVoid(Method m) {
        XposedBridge.hookMethod(m, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                return null;
            }
        });
    }
    
    public void load(String pref, Callback cb) {
        if (xsp.getBoolean(pref, false)) {
            try {
                cb.onCallback();
                nohengheng++;
            } catch (Exception e) {
                XposedBridge.log(e);
                aaaa++;
            }
        }
    }
    
}
