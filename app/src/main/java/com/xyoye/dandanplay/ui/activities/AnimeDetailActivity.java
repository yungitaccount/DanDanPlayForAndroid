package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.xyoye.core.adapter.BaseRvAdapter;
import com.xyoye.core.base.BaseActivity;
import com.xyoye.core.interf.AdapterItem;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.AnimeDetailBean;
import com.xyoye.dandanplay.bean.event.SearchMagnetEvent;
import com.xyoye.dandanplay.mvp.impl.AnimeDetailPresenterImpl;
import com.xyoye.dandanplay.mvp.presenter.AnimeDetailPresenter;
import com.xyoye.dandanplay.mvp.view.AnimeDetailView;
import com.xyoye.dandanplay.ui.weight.CornersCenterCrop;
import com.xyoye.dandanplay.ui.weight.ExpandableTextView;
import com.xyoye.dandanplay.ui.weight.ScrollableHelper;
import com.xyoye.dandanplay.ui.weight.ScrollableLayout;
import com.xyoye.dandanplay.ui.weight.item.AnimeEpisodeItem;
import com.xyoye.dandanplay.utils.UserInfoShare;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * Created by YE on 2018/7/20.
 */

public class AnimeDetailActivity extends BaseActivity<AnimeDetailPresenter> implements AnimeDetailView, ScrollableHelper.ScrollableContainer{
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolBar;
    @BindView(R.id.scroll_layout)
    ScrollableLayout scrollableLayout;
    @BindView(R.id.anima_image_iv)
    ImageView animaImageIv;
    @BindView(R.id.anima_title_tv)
    TextView animaTitleTv;
    @BindView(R.id.anima_rating_tv)
    TextView animaTRatingTv;
    @BindView(R.id.anima_onair_tv)
    TextView animaOnairTv;
    @BindView(R.id.anima_airday_tv)
    TextView animaAirdayTv;
    @BindView(R.id.anima_favorited_tv)
    TextView animaFavoritedTv;
    @BindView(R.id.anima_restricted_tv)
    TextView animaRestrictedTv;
    @BindView(R.id.anima_intro_tv)
    ExpandableTextView animaIntroTv;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    MenuItem favoriteItem = null;
    private AnimeDetailBean animeDetailBean;

    private boolean isFavorite = false;
    private String animaId = "";
    private BaseRvAdapter<AnimeDetailBean.BangumiBean.EpisodesBean> adapter;

    @Override
    public void initView() {
        setTitle(R.string.anime_detail_title);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    }

    @Override
    public void initListener() {

    }

    @NonNull
    @Override
    protected AnimeDetailPresenter initPresenter() {
        return new AnimeDetailPresenterImpl(this, this);
    }

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_anime_detail;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.favorite:
                if (UserInfoShare.getInstance().isLogin()){
                    if (isFavorite){
                        favoriteCancel();
                    }else {
                        favoriteConfirm();
                    }
                }else {
                    ToastUtils.showShort(R.string.anime_detail_not_login_hint);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorite, menu);
        favoriteItem = menu.findItem(R.id.favorite);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public String getAnimaId() {
        animaId = getIntent().getStringExtra("animaId");
        return animaId;
    }

    @Override
    public void showAnimeDetail(AnimeDetailBean bean) {
        if(this.isFinishing())
            return;

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(new CornersCenterCrop(10));
        Glide.with(this)
                .load(bean.getBangumi().getImageUrl())
                .apply(options)
                .into(animaImageIv);

        animeDetailBean = bean;
        animaTitleTv.setText(bean.getBangumi().getAnimeTitle());
        animaTRatingTv.setText("评分："+String.valueOf(bean.getBangumi().getRating()));
        animaOnairTv.setText(bean.getBangumi().isIsOnAir()
                            ? "状态：连载中"
                            : "状态：完结");
        animaAirdayTv.setText(getChineseText(bean.getBangumi().getAirDay()));
        animaFavoritedTv.setText(bean.getBangumi().isIsFavorited()
                                ? "已关注"
                                : "未关注");

        if (bean.getBangumi().isIsFavorited()){
            if (favoriteItem != null){
                favoriteItem.setTitle("取消关注");
                isFavorite = true;
            }
        }else {
            if (favoriteItem != null){
                favoriteItem.setTitle("关注");
                isFavorite = false;
            }
        }

        if (bean.getBangumi().isIsRestricted())
            animaRestrictedTv.setVisibility(View.VISIBLE);
        else
            animaRestrictedTv.setVisibility(View.GONE);

        animaIntroTv.setText("简介："+bean.getBangumi().getSummary());

        //剧集倒序
        List<AnimeDetailBean.BangumiBean.EpisodesBean> episodesList = bean.getBangumi().getEpisodes();
        Collections.reverse(episodesList);
        adapter = new BaseRvAdapter<AnimeDetailBean.BangumiBean.EpisodesBean>(episodesList) {
            @NonNull
            @Override
            public AdapterItem<AnimeDetailBean.BangumiBean.EpisodesBean> onCreateItem(int viewType) {
                return new AnimeEpisodeItem();
            }
        };
        recyclerView.setAdapter(adapter);
        scrollableLayout.getHelper().setCurrentScrollableContainer(this);
    }

    private String getChineseText(int day){
        switch (day){
            case 0:
                return "日期：星期日";
            case 1:
                return "日期：星期一";
            case 2:
                return "日期：星期二";
            case 3:
                return "日期：星期三";
            case 4:
                return "日期：星期四";
            case 5:
                return "日期：星期五";
            case 6:
                return "日期：星期六";
            default:
                return "日期：未知";
        }
    }

    @Override
    public View getScrollableView() {
        return recyclerView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SearchMagnetEvent event){
        String episode = event.getEpisodeName();
        if (episode.startsWith("第") && episode.endsWith("话")){
            String temp = episode.substring(1, episode.length()-1);
            episode = isInt(temp) ? temp : episode;
        }
        Intent intent = new Intent(AnimeDetailActivity.this, SearchMagnetActivity.class);
        intent.putExtra("anime", animeDetailBean.getBangumi().getSearchKeyword());
        intent.putExtra("anime_title", "/"+animeDetailBean.getBangumi().getAnimeTitle());
        intent.putExtra("episode_title", episode);
        intent.putExtra("episode_id", event.getEpisodeId());
        startActivity(intent);
    }

    private void favoriteConfirm(){
        AnimeDetailBean.addFavorite(animaId, new CommJsonObserver<CommJsonEntity>(this) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                if (favoriteItem != null){
                    isFavorite = true;
                    favoriteItem.setTitle("取消关注");
                    animaFavoritedTv.setText("已关注");
                    ToastUtils.showShort("关注成功");
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                TLog.e(message);
            }
        }, new NetworkConsumer());
    }

    private void favoriteCancel(){
        AnimeDetailBean.reduceFavorite(animaId, new CommJsonObserver<CommJsonEntity>(this) {
            @Override
            public void onSuccess(CommJsonEntity commJsonEntity) {
                if (favoriteItem != null){
                    isFavorite = false;
                    favoriteItem.setTitle("关注");
                    animaFavoritedTv.setText("未关注");
                    ToastUtils.showShort("取消关注成功");
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
                TLog.e(message);
            }
        }, new NetworkConsumer());
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
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

    }

    private static boolean isInt(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
