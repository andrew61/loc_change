package studios.gaberlunzie.locchange;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import studios.gaberlunzie.locchange.models.SavedLocation;

/**
 * Created by Andrew on 1/21/2018.
 */

public class LocationsActivity extends Activity {
    private final Context context = this;

    private ListView mListView;
    private Button startBtn;
    private Button stopBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);
        mListView = (ListView) findViewById(R.id.list);
        stopBtn = (Button) findViewById(R.id.btn_stop);
        startBtn = (Button) findViewById(R.id.btn_start);

        final ArrayList<SavedLocation> savedLocations = (ArrayList<SavedLocation>)UserSession.getSavedLocations(AppDatabase.getAppDatabase(this));

        LocationsAdapter locationsAdapter = new LocationsAdapter(this, savedLocations, mListView, this);
        mListView.setAdapter(locationsAdapter);
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                SavedLocation savedLocation = savedLocations.get(i);
//                UserSession.setSavedLocation(savedLocation);
//
//                finish();
//            }
//        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LocationService.class);
                stopService(intent);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LocationService.class);
                startService(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

class LocationsAdapter extends BaseAdapter {

    private ListView mListView;
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<SavedLocation> mDataSource;
    private Activity mActivity;

    public LocationsAdapter(Context context, ArrayList<SavedLocation> items, ListView listView, Activity activity) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListView = listView;
        mActivity = activity;
    }

    public LocationsAdapter(Context context, ArrayList<SavedLocation> items, ListView listView) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListView = listView;
    }
    //1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        ViewHolder holder;

        // check if the view already exists if so, no need to inflate and findViewById again!
        if (convertView == null) {

            // Inflate the custom row layout from your XML.
            convertView = mInflater.inflate(R.layout.list_item_location, parent, false);

            // create a new "Holder" with subviews
            holder = new ViewHolder();
            holder.titleBtn = (Button) convertView.findViewById(R.id.titleBtn);
            holder.deleteBtn = (Button) convertView.findViewById(R.id.deleteBtn);

            holder.titleBtn.setOnClickListener(onTitleBtnClickListener);
            holder.deleteBtn.setOnClickListener(onDeleteBtnClickListener);

            // hang onto this holder for future recyclage
            convertView.setTag(holder);
        }
        else {

            // skip all the expensive inflation/findViewById and just get the holder you already made
            holder = (ViewHolder) convertView.getTag();
        }

        Button interactionView = holder.titleBtn;
        SavedLocation savedLocation = (SavedLocation) getItem(position);

        interactionView.setText(savedLocation.getLocationName());

        return convertView;
    }

    class ViewHolder {
        public Button titleBtn;
        public Button deleteBtn;
    }

    private void reloadData(){
        final ArrayList<SavedLocation> savedLocations = (ArrayList<SavedLocation>)UserSession.getSavedLocations(AppDatabase.getAppDatabase(mContext));

        LocationsAdapter locationsAdapter = new LocationsAdapter(mContext, savedLocations, mListView);
        mListView.setAdapter(locationsAdapter);
        locationsAdapter.notifyDataSetChanged();
    }

    View.OnClickListener onTitleBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final int position = mListView.getPositionForView((View) view.getParent());
            SavedLocation savedLocation = mDataSource.get(position);
            UserSession.setSavedLocation(savedLocation);
            if(mActivity != null){
                mActivity.finish();
            }
        }
    };

    View.OnClickListener onDeleteBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final int position = mListView.getPositionForView((View) view.getParent());
            final SavedLocation savedLocation = mDataSource.get(position);
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

            alert.setTitle("Delete?");

            alert.setMessage(savedLocation.getLocationName());

            alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    UserSession.deleteSavedLocation(AppDatabase.getAppDatabase(mContext), savedLocation);
                    reloadData();
                }
            });

            alert.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });

            alert.show();
        }
    };
}
