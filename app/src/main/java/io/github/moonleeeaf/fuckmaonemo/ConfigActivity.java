package io.github.moonleeeaf.fuckmaonemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toolbar;

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
    }
    
}
