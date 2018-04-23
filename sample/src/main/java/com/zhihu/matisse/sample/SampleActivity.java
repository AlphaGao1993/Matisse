/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private UriAdapter mAdapter;
    private ArrayList<Uri> uris = new ArrayList<>();
    private ArrayList<String> paths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.zhihu).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());
    }

    @Override
    public void onClick(final View v) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            switch (v.getId()) {
                                case R.id.zhihu:
                                    Matisse.from(SampleActivity.this)
                                            .choose(MimeType.ofImage())
                                            .groupByDate(true)
                                            .countable(true)
                                            //.hideCheckViewOnSingleMode(true)
                                            .maxSelectable(80)
                                            //.capture(false)
                                            .spanCount(4)
                                            //.captureStrategy(new CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider"))
                                            //.addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                            //.gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                            .applyString("发布")
                                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .theme(R.style.Matisse_Zhihu)
                                            //.selectedUris(uris)
                                            .thumbnailScale(0.85f)
                                            .imageEngine(new PicassoLoader())
                                            .forResult(REQUEST_CODE_CHOOSE);
/*                                    Matisse.from(SampleActivity.this)
                                            .choose(MimeType.of(MimeType.JPEG, MimeType.PNG))
                                            .groupByDate(true)
                                            .countable(false)
                                            .maxSelectable(80)
                                            .spanCount(4)
                                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                            .theme(R.style.Matisse_Zhihu)
                                            .selectedUris(uris)
                                            .thumbnailScale(0.55f)
                                            .imageEngine(new PicassoLoader())
                                            .forResult(REQUEST_CODE_CHOOSE);*/
                                    break;
                                case R.id.dracula:
                                    Matisse.from(SampleActivity.this)
                                            .choose(MimeType.ofImage())
                                            .theme(R.style.Matisse_Dracula)
                                            .countable(false)
                                            .selectedUris(uris)
                                            .groupByDate(true)
                                            .maxSelectable(1)
                                            .imageEngine(new PicassoLoader())
                                            .forResult(REQUEST_CODE_CHOOSE);
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            Toast.makeText(SampleActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            uris = (ArrayList<Uri>) Matisse.obtainResult(data);
            paths = (ArrayList<String>) Matisse.obtainPathResult(data);
            mAdapter.setData(uris, paths);
        }
        TextView tv = findViewById(R.id.images_count);
        tv.setText("数量：" + uris.size());
    }

    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = contentView.findViewById(R.id.uri);
                mPath = contentView.findViewById(R.id.path);
            }
        }
    }

}
