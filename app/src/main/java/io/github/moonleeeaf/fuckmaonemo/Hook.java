package io.github.moonleeeaf.fuckmaonemo;
import android.app.Activity;
import android.app.Application;
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

public class Hook implements IXposedHookLoadPackage {
    private static boolean isHooked = false;
    private XSharedPreferences xsp;
    
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
                        hook(param, initApp.getClass().getClassLoader());
                    }
                    
                }
            );
        }
    }
    
    public static Method getMethod(Class clazz, String name, Class... args) throws NoSuchMethodException {
        return clazz.getDeclaredMethod(name, args);
    }
   
    public Application getApplication() throws ClassNotFoundException {
        return (Application) XposedHelpers.callStaticMethod(Class.forName("android.app.ActivityThread"), "currentApplication");
    }
    
    public void hook(XC_LoadPackage.LoadPackageParam param, ClassLoader classLoader) throws Exception {
        if (isHooked) return;
        else isHooked = true;
        
        int nohengheng = 0;
        
        xsp = new XSharedPreferences("io.github.moonleeeaf.fuckmaonemo", "config");
        
        XposedBridge.log("[FuckMaoNemo] 注入中...");
        
        // 拦截40x码
        if (xsp.getBoolean("fuck_40x", false)) {
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
                            XposedBridge.log("[FuckMaoNemo] 拦截响应 40x 码");
                            String t = "";
                            switch (code){
                                case 401:
                                    t = "已拦截异常登出";
                                    break;
                                case 422:
                                    t = "已拦截封号页面替换资料卡";
                                    break;
                                case 405:
                                    t = "评论接口被禁止";
                                    break;
                                default:
                                    t = "未知拦截，响应码为 " + code;
                            }
                            Toast.makeText(getApplication(), "[FuckMaoNemo] " + t, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            );
            nohengheng++;
        }
        
        // 绕过防沉迷
        if (xsp.getBoolean("fuck_fcm", false)) {
            XposedBridge.log("[FuckMaoNemo] Hook_绕过防沉迷");
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.activity.WorkDetailActivity", classLoader),
                    "checkAntiAddictionState",
                    null
                ),
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                        XposedBridge.log("[FuckMaoNemo] 拦截防沉迷方法调用");
                        return null;
                    }
                    
                }
            );
            nohengheng++;
        }
        
        XposedBridge.log("[FuckMaoNemo] 执行完毕");
        
        Toast.makeText(getApplication(), "[FuckMaoNemo] 加载成功 (≧▽≦)\n" + nohengheng + " 个功能已加载", Toast.LENGTH_LONG).show();
    }
    
}
