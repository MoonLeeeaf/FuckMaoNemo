package io.github.moonleeeaf.fuckmaonemo;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
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
    
    public static final String MIAO_LIST = "妈 马 操 草 傻 艹 牛 逼 P 槽 涩 色 m";
    public static final String[] MIAO = MIAO_LIST.split(" ");
    
    private XC_MethodHook.Unhook force_set_work_myown_unhook;
    
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
    
    public static String fuck屏蔽词(String str) {
        for (String i : MIAO) {
            str = str.replaceAll(i, "‌" + i + "‌");
        }
        return str;
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
                            switch (code) {
                                case 401:
                                    t = "已阻止异常登出";
                                    break;
                                case 405:
                                case 422:
                                    t = "API 访问被拒绝，评论区或者已封禁账号？";
                                    break;
                            }
                            Toast.makeText(getApplication(), "[FuckMaoNemo] " + t, Toast.LENGTH_SHORT).show();
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
        
        // 屏蔽更新
        load("fuck_update", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_屏蔽更新");
            methodToVoid(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.sdk.update.NetChangeReceiver", classLoader),
                    "onReceive",
                    Context.class,
                    Intent.class
                )
            );
        });
        
        // 强制置顶评论
        load("force_top_comment", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_强制置顶评论");
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.view.CommentOptionDialogV2", classLoader),
                    "checkIsSelf",
                    null
                ),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam mp) throws Throwable {
                        XposedHelpers.setBooleanField(mp.thisObject, "isOwnWork", true);
                    }
                }
            );
        });
        
        // 谋权篡位
        load("force_set_work_myown", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_谋权篡位");
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.activity.WorkDetailActivity", classLoader),
                    "setWorkDetailData",
                    null
                ),
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam mp) throws Throwable {
                        force_set_work_myown_unhook = XposedBridge.hookMethod(
                        getMethod(
                            XposedHelpers.findClass("com.codemao.creativecenter.utils.bcm.bean.AuthorInfo", classLoader),
                            "getId",
                            null
                        ),
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam hp) throws Throwable {
                                Method m = getMethod(XposedHelpers.findClass("com.codemao.nemo.util.LocalUserHelper", classLoader), "getUserInfo", null);
                                m.setAccessible(true);
                                Object usrInfo = m.invoke(null, null);
                                        
                                m = getMethod(usrInfo.getClass(), "getId", null);
                                m.setAccessible(true);
                                        
                                force_set_work_myown_unhook.unhook(); // 希望人没事
                                return m.invoke(usrInfo, null); // long 类型
                            }
                        });
                    }
                }
            );
        });
        
        // 防止屏蔽屏蔽词
        load("fuck_miao", () -> {
            XposedBridge.log("[FuckMaoNemo] Hook_反屏蔽");
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam mp) throws Throwable {
                    XposedHelpers.setObjectField(mp.thisObject, "content", fuck屏蔽词((String) XposedHelpers.getObjectField(mp.thisObject, "content")));
                }
            };
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.fragment.WorkCommentFragment", classLoader),
                    "sendReply",
                    null
                ),
                hook
            );
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.fragment.WorkCommentFragment", classLoader),
                    "sendComment",
                    null
                ),
                hook
            );
            XposedBridge.hookMethod(
                getMethod(
                    XposedHelpers.findClass("com.codemao.nemo.activity.CommentDetailActivity", classLoader),
                    "send",
                    null
                ),
                hook
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
