package com.tianxia.lib.baseworld2.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.tianxia.lib.baseworld2.BaseApplication;
import com.tianxia.lib.baseworld2.R;
import com.tianxia.lib.baseworld2.cache.ConfigCache;
import com.tianxia.lib.baseworld2.sync.http.AsyncHttpClient;
import com.tianxia.lib.baseworld2.sync.http.AsyncHttpResponseHandler;
import com.tianxia.lib.baseworld2.utils.NetworkUtils;
import com.tianxia.widget.image.SmartImageView;

public class ArchiverPageActivity extends AdapterActivity<StatuInfo>
    implements View.OnClickListener {

    private TextView mAppTitle;
    private Button mAppHeaderBack;
    private View mAppHeaderBackDivider;

    private int mPage;

    private TextView mItemName;
    private TextView mItemText;
    private SmartImageView mItemMiddle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPageList(mPage);
    }

    @Override
    protected void setLayoutView() {
        setContentView(R.layout.archiver_page);
        setListView(R.id.archiver_page_list);

        mPage = getIntent().getIntExtra("page", 0);

        mAppTitle = (TextView) findViewById(R.id.app_title);
        mAppHeaderBack = (Button) findViewById(R.id.app_header_back);
        mAppHeaderBackDivider = findViewById(R.id.app_header_back_divider);

        mAppTitle.setText(getString(R.string.archiver_page_title, mPage));
        mAppHeaderBack.setVisibility(View.VISIBLE);
        mAppHeaderBackDivider.setVisibility(View.VISIBLE);

        mAppHeaderBack.setOnClickListener(this);

        showLoadingEmptyView();
    }

    private void setPageList(int pageIndex) {
        final String pageUrl = BaseApplication.mServerPageUrl + pageIndex + ".json";
        String cacheConfigString = ConfigCache.getUrlCache(pageUrl);
        if (cacheConfigString != null) {
            try {
                showSeasonList(cacheConfigString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // if network is unavaliable, just show fail at once
            if (BaseApplication.mNetWorkState == NetworkUtils.NETWORN_NONE) {
                listView.setAdapter(null);
                showFailEmptyView();
                return;
            }

            AsyncHttpClient client = new AsyncHttpClient();
            client.get(pageUrl, new AsyncHttpResponseHandler(){

                @Override
                public void onStart() {
                }

                @Override
                public void onSuccess(String result){
                    try {
                        showSeasonList(result);
                        ConfigCache.setUrlCache(result, pageUrl);
                    } catch (Exception e) {
                        listView.setAdapter(null);
                        showFailEmptyView();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable arg0) {
                    listView.setAdapter(null);
                    showFailEmptyView();
                }

            });
        }
    }

    private void showSeasonList(String result) throws JSONException {
        JSONObject statusConfig = new JSONObject(result);

        JSONArray statuList = statusConfig.getJSONArray("statuses");
        StatuInfo statuInfo = null;
        for (int i = statuList.length() - 1; i >= 0; i--) {
            statuInfo = new StatuInfo();
            statuInfo.name = statuList.getJSONObject(i).getString("name");
            statuInfo.author = statuList.getJSONObject(i).getString("author");
            statuInfo.text = statuList.getJSONObject(i).getString("text");
            statuInfo.id = statuList.getJSONObject(i).getLong("id");
            statuInfo.pic_thumbnail = statuList.getJSONObject(i).optString("pic_thumbnail");

            listData.add(statuInfo);
        }
        adapter = new Adapter(ArchiverPageActivity.this);
        listView.setAdapter(adapter);
    }

    @Override
    protected View getView(int position, View convertView) {
        View view = convertView;
        if(view == null){
            view = LayoutInflater.from(this).inflate(R.layout.archiver_page_list_item, null);
        }
        mItemName = (TextView) view.findViewById(R.id.item_name);
        mItemName.setText(listData.get(position).name);
        mItemName.getPaint().setFakeBoldText(true);

        mItemText = (TextView) view.findViewById(R.id.item_text);
        mItemText.setText(listData.get(position).text);

        mItemMiddle = (SmartImageView) view.findViewById(R.id.item_thumbnail);
        if (listData.get(position).pic_thumbnail != null && !"".equals(listData.get(position).pic_thumbnail)) {
            mItemMiddle.setImageUrl(listData.get(position).pic_thumbnail, R.drawable.icon, 0);
            mItemMiddle.setVisibility(View.VISIBLE);
        } else {
            mItemMiddle.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    protected void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        
    }

    @Override
    public void onClick(View v) {
        if (v == mAppHeaderBack) {
            onBackPressed();
        }
    }
}
