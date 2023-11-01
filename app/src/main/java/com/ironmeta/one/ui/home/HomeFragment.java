//package com.ironmeta.one.ui.home;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.graphics.Paint;
//import android.os.Bundle;
//import android.text.Spannable;
//import android.text.SpannableStringBuilder;
//import android.text.Spanned;
//import android.text.format.Formatter;
//import android.text.style.ForegroundColorSpan;
//import android.text.style.RelativeSizeSpan;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.Keep;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.FragmentActivity;
//import androidx.lifecycle.LiveData;
//import androidx.lifecycle.ViewModelProvider;
//
//import com.bumptech.glide.Glide;
//import com.ironmeta.base.vstore.VstoreManager;
//import com.ironmeta.one.MainActivity;
//import com.ironmeta.one.R;
//import com.ironmeta.one.base.utils.ToastUtils;
//import com.ironmeta.one.coreservice.CoreServiceManager;
//import com.ironmeta.one.combo.bean.VAdMaxData;
//import com.ironmeta.one.comboads.helper.VadPresenterWrapper;
//import com.ironmeta.one.report.AppReport;
//import com.ironmeta.one.ui.common.CommonDialog;
//import com.ironmeta.one.ui.common.CommonFragment;
//import com.ironmeta.one.ui.dialog.CongratulationsDialog;
//import com.ironmeta.one.ui.helper.AddTimeHelper;
//import com.ironmeta.one.ui.helper.NetworkRateHelper;
//import com.ironmeta.tahiti.TahitiCoreServiceStateInfoManager;
//import com.ironmeta.tahiti.constants.CoreServiceErrorConstants;
//import com.ironmeta.tahiti.constants.CoreServiceStateConstants;
//
//// https://issuetracker.google.com/issues/142601969
//@Keep
//public class HomeFragment extends CommonFragment implements View.OnClickListener {
//
//    private void initAddTime() {
//
//
//        btnAddTimeTwoHours.setOnClickListener(v -> {
//            VadPresenterWrapper vadPresenterWrapper2 = getVadPresenterWrapper();
//            if (vadPresenterWrapper2 == null) {
//                return;
//            }
//            LiveData<Boolean> showRewardedAdResultAsLiveData = vadPresenterWrapper2.showRewardedAd(VadConstants.LOCATION.REWARDED_TO_ADD_TIME);
//            if (showRewardedAdResultAsLiveData == null) {
//                return;
//            }
//            showRewardedAdResultAsLiveData.observe(getViewLifecycleOwner(), showRewardedAdResult -> {
//                if (showRewardedAdResult != null && showRewardedAdResult) {
//                    TahitiCoreServiceStateInfoManager.getInstance(requireContext()).addUsedUpMinutes(120);
//                    showToast(getResources().getString(R.string.vs_add_time_two_hour_succeed));
//                } else {
//                    showToast(getResources().getString(R.string.vs_add_time_two_hour_fail));
//                }
//            });
//        });
//    }
//
//}
