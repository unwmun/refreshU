package com.unw.refreshu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by unw on 15. 4. 19..
 */
public class RegisterActivity extends Activity
{
    private static final String TAG = "RegisterActivity";

    private ListView mListView;

    private List<ActivityInfo> mActivityInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        mListView = (ListView) findViewById(R.id.register_listview);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String json = settings.getString(getString(R.string.setting_activity_infos_key), "");
        mActivityInfos = Util.transActivityInfoJsonToList(json);
        mListView.setAdapter(new RegisteredActivityAdapter(this, 0, mActivityInfos));

    }

    private class RegisteredActivityAdapter extends ArrayAdapter<ActivityInfo>
    {
        private final String[] TITLES = new String[]{
            "RIDI", "KYOBO", "CREMA"
        };

        public RegisteredActivityAdapter(Context context, int resource, List<ActivityInfo> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_registered_activity, parent, false);
            }

            final ActivityInfo info = getItem(position);

            TextView titleLabel = ViewHolder.get(convertView, R.id.tv_activity_title);
            String title = info.getPackageName();
            for (String t : TITLES) {
                if (info.getPackageName().contains(t.toLowerCase())) {
                    title = t;
                    break;
                }
            }
            titleLabel.setText(title);

            ViewHolder.get(convertView, R.id.tv_activity_delete_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mActivityInfos.remove(info);
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                    settings.edit()
                            .putString(getString(R.string.setting_activity_infos_key), Util.transListToJson(mActivityInfos))
                            .commit();

                    notifyDataSetChanged();
                }
            });

            return convertView;
        }
    }

}
