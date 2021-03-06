package com.xyoye.dandanplay.ui.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.base.BaseMvpActivity;
import com.xyoye.dandanplay.bean.params.ChangePasswordParam;
import com.xyoye.dandanplay.mvp.impl.ChangePasswordPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.ChangePasswordPresenter;
import com.xyoye.dandanplay.mvp.view.ChangePasswordView;
import com.xyoye.dandanplay.ui.weight.dialog.ToLoginDialog;
import com.xyoye.dandanplay.utils.AppConfig;

import butterknife.BindView;

/**
 * Created by YE on 2018/8/11.
 */

public class ChangePasswordActivity extends BaseMvpActivity<ChangePasswordPresenter> implements ChangePasswordView, View.OnClickListener {
    @BindView(R.id.return_iv)
    ImageView returnIv;
    @BindView(R.id.user_old_password_et)
    TextInputEditText oldPasswordEt;
    @BindView(R.id.user_new_password_et)
    TextInputEditText newPasswordEt;
    @BindView(R.id.user_old_password_layout)
    TextInputLayout oldPasswordLayout;
    @BindView(R.id.user_new_password_layout)
    TextInputLayout newPasswordLayout;
    @BindView(R.id.change_bt)
    Button changeBt;

    @Override
    public void initView() {
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        Window window = getWindow();
        window.setFlags(flag, flag);
    }

    @Override
    public void initListener() {
        returnIv.setOnClickListener(this);
        changeBt.setOnClickListener(this);
    }

    @NonNull
    @Override
    protected ChangePasswordPresenter initPresenter() {
        return new ChangePasswordPresenterImpl(this,this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_change_password;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.return_iv:
                ChangePasswordActivity.this.finish();
                break;
            case R.id.change_bt:
                change();
                break;
        }
    }

    private void change(){

        String oldPassword = oldPasswordEt.getText().toString();
        String newPassword = newPasswordEt.getText().toString();

        if (StringUtils.isEmpty(oldPassword)) {
            oldPasswordLayout.setErrorEnabled(true);
            oldPasswordLayout.setError("旧密码不能为空");
            return;
        }
        if (StringUtils.isEmpty(newPassword)) {
            newPasswordLayout.setErrorEnabled(true);
            newPasswordLayout.setError("新密码不能为空");
            return;
        }
        if (oldPassword.length()<5 || oldPassword.length()>20) {
            oldPasswordLayout.setErrorEnabled(true);
            oldPasswordLayout.setError("旧密码长度为5-20个字符");
            return;
        }
        if (newPassword.length()<5 || newPassword.length()>20) {
            newPasswordLayout.setErrorEnabled(true);
            newPasswordLayout.setError("新密码长度为5-20个字符");
            return;
        }

        oldPasswordLayout.setErrorEnabled(false);
        newPasswordLayout.setErrorEnabled(false);
        presenter.change(new ChangePasswordParam(oldPassword, newPassword));
    }

    @Override
    public void changeSuccess() {
        ToLoginDialog dialog = new ToLoginDialog(this, R.style.Dialog, 2, () -> {
            AppConfig.getInstance().setLogin(false);
            AppConfig.getInstance().saveUserName("");
            AppConfig.getInstance().saveUserScreenName("");
            AppConfig.getInstance().saveUserImage("");
            AppConfig.getInstance().saveToken("");
            if (ActivityUtils.isActivityExistsInStack(PersonalInfoActivity.class))
                ActivityUtils.finishActivity(PersonalInfoActivity.class);
            IApplication.isUpdateUserInfo = true;
            launchActivity(LoginActivity.class);
            ChangePasswordActivity.this.finish();
        });
        dialog.show();
    }

    @Override
    public void showLoading() {
        showLoadingDialog();
    }

    @Override
    public void hideLoading() {
        dismissLoadingDialog();
    }

    @Override
    public void showError(String message) {
        ToastUtils.showShort(message);
    }
}
