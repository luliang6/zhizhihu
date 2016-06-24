package com.lanjiaai.kzhihu.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.lanjiaai.kzhihu.R;
import com.lanjiaai.kzhihu.model.api.ZhiHu;
import com.lanjiaai.kzhihu.model.entity.Answer;
import com.lanjiaai.kzhihu.ui.activity.base.SwipeBackActivity;
import com.lanjiaai.kzhihu.ui.adapter.PostAnswerAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class MyFavoriteActivity extends SwipeBackActivity {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.progress)
    ProgressBar progress;
    @Bind(R.id.my_favorite_answer_list_view)
    RecyclerView myFavoriteAnswerListView;

    private Realm mRealm;
    private PostAnswerAdapter postAnswerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_favorite);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("我的收藏");

        mRealm = Realm.getDefaultInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        myFavoriteAnswerListView.setLayoutManager(linearLayoutManager);
        myFavoriteAnswerListView.hasFixedSize();

    }

    @Override
    protected void onResume() {
        super.onResume();

        List<Answer> answers = mRealm.allObjects(Answer.class);
        initList(answers);
    }


    public void initList(final List<Answer> answers) {
        postAnswerAdapter = new PostAnswerAdapter(this, answers);
        postAnswerAdapter.setOnItemClickListener(getOnItemClickListener(answers));
        myFavoriteAnswerListView.setAdapter(postAnswerAdapter);
        progress.setVisibility(View.GONE);
    }

    private PostAnswerAdapter.OnItemClickListener getOnItemClickListener(final List<Answer> answers) {
        return new PostAnswerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Answer answer = answers.get(position);
                String qId = answer.getQuestionid();
                String aId = answer.getAnswerid();
                String url = String.format(ZhiHu.QUESTION_BASE_URL + "%s/answer/%s", qId, aId);
                Intent intent = new Intent(getContext(), WebBrowserActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);

//                boolean hasZhiHuClient = isAvilible(PostAnswersActivity.this, ZhiHu.PACKAGE_NAME);
//                if (hasZhiHuClient) {
//                    Log.e("onItemClick=>", "hasZhiHuClient");
//                    String url = String.format(ZhiHu.QUESTION_BASE_URL + "%s/answer/%s", qId, aId);
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    intent.setPackage(ZhiHu.PACKAGE_NAME);
//                    startActivity(intent);
//                } else {
//                    toast("请您安装知乎客户端");
//                }
            }

            @Override
            public void onFavoriteClick(View view, int position) {
                Answer answer = answers.get(position);
                Answer favoriteAnswer = Realm.getDefaultInstance()
                        .where(Answer.class)
                        .equalTo("answerid", answer.getAnswerid())
                        .findFirst();
                if (favoriteAnswer != null) {
                    // remove
                    mRealm.beginTransaction();
                    favoriteAnswer.removeFromRealm();
                    mRealm.commitTransaction();
                    postAnswerAdapter.notifyDataSetChanged();
                    toast("取消收藏");
                } else {
                    // save
                    mRealm.beginTransaction();
                    mRealm.copyToRealm(answer);
                    mRealm.commitTransaction();
                    postAnswerAdapter.notifyDataSetChanged();
                    toast("已收藏");
                }
            }
        };
    }
}