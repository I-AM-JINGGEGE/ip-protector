package com.ironmeta.one.ui.support;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ironmeta.one.R;
import com.ironmeta.one.ui.adapter.LanguageSettingRecyclerViewAdapter;
import com.ironmeta.one.ui.common.CommonAppCompatActivity;
import com.ironmeta.one.ui.helper.LanguageSettingHelper;

public class LanguageSettingActivity extends CommonAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_setting);

        initView();
    }

    /**
     * init view begin
     */
    private RecyclerView mRecycleViewLanguageSetting;

    private void initView() {
        // toolbar
        ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> onBackPressed());

        mRecycleViewLanguageSetting = findViewById(R.id.recycler_view_language_setting);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecycleViewLanguageSetting.setLayoutManager(linearLayoutManager);
        LanguageSettingRecyclerViewAdapter adapter = new LanguageSettingRecyclerViewAdapter(this);
        mRecycleViewLanguageSetting.setAdapter(adapter);
    }
    /** init view end **/

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}