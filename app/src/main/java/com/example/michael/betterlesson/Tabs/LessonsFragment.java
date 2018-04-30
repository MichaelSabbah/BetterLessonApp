package com.example.michael.betterlesson.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.example.michael.betterlesson.Adapters.LessonsAdapter;
import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.LessonPageActivity;
import com.example.michael.betterlesson.Logic.Lesson;
import com.example.michael.betterlesson.Logic.TimeFormats;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.MainActivity;
import com.example.michael.betterlesson.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;

public class LessonsFragment extends Fragment {

    private static final String CURRENT_USER= "currentUser";

    //Firebase
    private DatabaseReference mDatabase;

    //Views
    private TextView emptyLessonsListText;
    private ListView lessonsList;

    //Logic variables
    private ArrayList<Lesson> lessons;
    private User currentUser;
    private LessonsAdapter lessonAdapter;

    public LessonsFragment() {
        // Required empty public constructor
    }

    public static LessonsFragment newInstance(User currentUser) {
        LessonsFragment fragment = new LessonsFragment();
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
        View view = inflater.inflate(R.layout.fragment_lessons, container, false);

        emptyLessonsListText = (TextView)view.findViewById(R.id.empty_lessons_list_text);
        lessonsList = (ListView)view.findViewById(R.id.lessons_list_view);

        lessonsList.setOnScrollListener(new ListView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(lessonsList.getChildAt(0).getTop() == 0)
                    MainActivity.getFab().show();
                else
                    MainActivity.getFab().hide();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        lessonsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent lessonPageIntent = new Intent(getActivity(), LessonPageActivity.class);
                lessonPageIntent.putExtra("lesson",lessons.get(position));
                lessonPageIntent.putExtra("currentUserType",currentUser.getType());
                startActivity(lessonPageIntent);
            }
        });

        loadLessonsFromDatabase();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }

    public void loadLessonsFromDatabase(){
        final String currentUserEmailKey = DatabaseHandler.emailToValidKey(currentUser.getEmail());
        final String currentUserGroup = currentUser.getType().getTypeGroupName();
        final ArrayList<String> lessonsId = new ArrayList<String>();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(currentUserGroup).child(currentUserEmailKey).child("lessons");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot lessonId : dataSnapshot.getChildren()){
                    lessonsId.add(lessonId.getKey());
                }

                mDatabase = FirebaseDatabase.getInstance().getReference().child("lessons");
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        lessons = new ArrayList<Lesson>();
                        for(DataSnapshot lessonId : dataSnapshot.getChildren()){
                            if(lessonsId.contains(lessonId.getKey())) {
                                Lesson lesson = lessonId.getValue(Lesson.class);
                                if(TimeFormats.isLessonPassed(lesson) && !lesson.isPassed()){
                                    setToPassedInDatabase(lesson);
                                    lesson.setPassed(true);
                                }
                                if (lesson != null)
                                    lessons.add(lesson);
                            }
                        }
                        if(!lessons.isEmpty()) {
                            lessonsList.setVisibility(View.VISIBLE);
                            emptyLessonsListText.setVisibility(View.GONE);
                        }else{
                            lessonsList.setVisibility(View.GONE);
                            emptyLessonsListText.setVisibility(View.VISIBLE);
                        }
                        Collections.sort(lessons);
                        lessonAdapter = new LessonsAdapter(getContext(),lessons,currentUser.getType());
                        lessonAdapter.notifyDataSetChanged();
                        lessonsList.setAdapter(lessonAdapter);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                lessonsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                //Set on item click
                lessonsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                        final int checkedCount = lessonsList.getCheckedItemCount();
                        mode.setTitle(checkedCount + " Selected");
                        lessonAdapter.toggleSelection(position);
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
                                SparseBooleanArray selected = lessonAdapter
                                        .getSelectedIds();
                                // Captures all selected ids with a loop
                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        Lesson selecteditem = (Lesson)lessonAdapter
                                                .getItem(selected.keyAt(i));
                                        // Remove selected items following the ids
                                        lessonAdapter.remove(selecteditem);
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
                        lessonAdapter.removeSelection();
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void setToPassedInDatabase(Lesson lesson){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("lessons").child(lesson.getId()).child("passed").setValue(true);
    }

}
