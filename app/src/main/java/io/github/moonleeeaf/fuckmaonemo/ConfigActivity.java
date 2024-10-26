package io.github.moonleeeaf.fuckmaonemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.Toolbar;
import java.io.FileOutputStream;

public class ConfigActivity extends PreferenceActivity {
   
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        
        getPreferenceManager().setSharedPreferencesName("config");
        
        // 理应会被 LSPosed Hook 的，不过兼容性未知
        getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
            
        addPreferencesFromResource(R.xml.config);
        
        findPreference("about").setOnPreferenceClickListener((p) -> {
            new AlertDialog.Builder(this)
                .setTitle("关于")
                .setMessage("FuckMaoNemo 是一个对 编程猫Nemo/点个猫 一些不合理设计的强制补充以及对其部分功能缺失进行弥补，并提供一些其他十分实用的功能，为人民服务，帮助大家(｡･ω･｡)\n作者：满月叶")
                .show();
                
            return false;
        });
        
        findPreference("see_miao").setOnPreferenceClickListener((p) -> {
            new AlertDialog.Builder(this)
                .setTitle("屏蔽词列表")
                .setMessage("当期列表：\n" + getPreferenceManager().getSharedPreferences().getString("MIAO_LIST_SHARED", "获取失败！"))
                .show();
                
            return false;
        });
        
        findPreference("set_newest_works_filter").setOnPreferenceClickListener((p) -> {
            EditText edit = new EditText(this);
                
            edit.setText(getPreferenceManager().getSharedPreferences().getString("newest_works_filter_rule_shared", "userId 823651139"));
                
            new AlertDialog.Builder(this)
                .setTitle("最新作品过滤规则")
                .setView(edit)
                .setPositiveButton("保存", (d, w) -> {
                    getPreferenceManager().getSharedPreferences().edit().putString("newest_works_filter_rule_shared", edit.getText().toString()).apply();
                })
                .setNegativeButton("取消", (d, w) -> {})
                .show();
                
            return false;
        });
        
        try {
            FileOutputStream fos = openFileOutput("fuck_miao.txt", MODE_WORLD_READABLE);
            String s = new String(getAssets().open("屏蔽词.txt").readAllBytes());
            getPreferenceManager().getSharedPreferences().edit().putString("MIAO_LIST_SHARED", s).apply();
        } catch(Exception e) {
            Toast.makeText(this, "更新屏蔽词列表失败！" + e, Toast.LENGTH_LONG).show();
        }
    }
    
}
