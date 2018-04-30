package com.example.michael.betterlesson.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.michael.betterlesson.Adapters.HwAdapter;
import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.HwPageActivity;
import com.example.michael.betterlesson.Logic.HW;
import com.example.michael.betterlesson.Logic.TimeFormats;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.MainActivity;
import com.example.michael.betterlesson.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class HomeWorkFragment extends Fragment {

    private static final String CURRENT_USER = "currentUser";

    //Locgic references
    private User currentUser;
    private ArrayList<HW> hwList;
    private HwAdapter hwAdapter;

    //Firebase
    private DatabaseReference mDatabase;

    //Views
    private ListView hwListView;
    private TextView emptyHwListText;

    public HomeWorkFragment() {

    }
    public static HomeWorkFragment newInstance(User currentUser) {
        HomeWorkFragment fragment = new HomeWorkFragment();
        Bundle args = new Bundle();
        args.putSerializable(CURRENT_USER, currentUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUser = (User)getArguments().getSerializable(CURRENT_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_work, container, false);

        hwListView = view.findViewById(R.id.hw_list_view);
        emptyHwListText = view.findViewById(R.id.empty_list_hw_text);

        hwListView.setOnScrollListener(new ListView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(hwListView.getChildAt(0).getTop() == 0)
                    MainActivity.getFab().show();
                else
                    MainActivity.getFab().hide();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        loadHwFromDatabase();

        return view;
    }

    public void loadHwFromDatabase(){
        final ArrayList<String> hwIds = new ArrayList<String>();
        String currentUserEmailKey = DatabaseHandler.emailToValidKey(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        String curretnUserGroup = currentUser.getType().getTypeGroupName();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(curretnUserGroup).child(currentUserEmailKey).child("hw");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot id : dataSnapshot.getChildren()){
                    hwIds.add(id.getKey());
                }

                mDatabase = FirebaseDatabase.getInstance().getReference().child("hw");
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        hwList = new ArrayList<HW>();
                        for(DataSnapshot hwId : dataSnapshot.getChildren()){
                            HW hw = hwId.getValue(HW.class);
                            if(isDatePassed(hw.getDate())){
                                setToExpiredInDatabase(hw);
                            }
                            if(hwIds.contains(hwId.getKey())) {
                                hwList.add(hwId.getValue(HW.class));
                            }
                        }
                        if(!hwList.isEmpty()) {
                            hwListView.setVisibility(View.VISIBLE);
                            emptyHwListText.setVisibility(View.GONE);
                        }else{
                            hwListView.setVisibility(View.GONE);
                            emptyHwListText.setVisibility(View.VISIBLE);
                        }

                        Collections.sort(hwList);
                        hwAdapter = new HwAdapter(getContext(), hwList,currentUser.getType());
                        hwAdapter.notifyDataSetChanged();
                        hwListView.setAdapter(hwAdapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                hwListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                //Set on item click
                hwListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                        final int checkedCount = hwListView.getCheckedItemCount();
                        mode.setTitle(checkedCount + " Selected");
                        hwAdapter.toggleSelection(position);
                    }

                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        getActivity().getMenuInflater().inflate(R.menu.delete_menu, menu);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.delete_button:
                                // Calls getSelectedIds method from ListViewAdapter Class
                                SparseBooleanArray selected = hwAdapter
                                        .getSelectedIds();
                                // Captures all selected ids with a loop
                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        HW selecteditem = (HW)hwAdapter
                                                .getItem(selected.keyAt(i));

                                        // Remove selected items following the ids
                                        hwIds.remove(selecteditem);
                                        hwList.remove(selecteditem);
                                        hwAdapter.remove(selecteditem);
                                    }
                                }
                                // Close CAB
                                mode.finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        hwAdapter.removeSelection();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        hwListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HW hw = hwList.get(position);
                Intent hwPageIntent = new Intent(getActivity(), HwPageActivity.class);
                hwPageIntent.putExtra("hw",hw);
                startActivity(hwPageIntent);
            }
        });
    }

    public boolean isDatePassed(String date){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("dd/MM/yyyy") ;
        Date expirationDate = null;
        Date currentDate = null;

        //Current date
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String dateAsString = TimeFormats.getDateFormat(year,month,day);

        try {
            expirationDate = timeFormat.parse(date);
            currentDate = timeFormat.parse(dateAsString);
        }catch (ParseException e){
            e.printStackTrace();
        }

        if(currentDate.after(expirationDate))
            return true;
        return false;
    }

    public void setToExpiredInDatabase(HW hw){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(hw.getHwId()).child("expired").setValue(true);
    }
}
