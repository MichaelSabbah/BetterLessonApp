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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.michael.betterlesson.Adapters.ContactsAdapter;
import com.example.michael.betterlesson.Firebase.DatabaseHandler;
import com.example.michael.betterlesson.Logic.User;
import com.example.michael.betterlesson.Logic.UserType;
import com.example.michael.betterlesson.MainActivity;
import com.example.michael.betterlesson.R;
import com.example.michael.betterlesson.UserProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContactsFragment extends Fragment {

    //UI references
    private ListView contactsList;
    private TextView emptyListText;
    private ProgressBar loadDetailsProgress;

    //Firebase
    private DatabaseReference mDatabase;

    // Fragment initialization parameters
    private static final String CURRENT_USER = "currentUser";
    private static final String CURRENT_USER_EMAIL = "currentUserEmail";
    private static final String NEW_CONTACT = "newContact";

    //Logic references
    private User currentUser;
    private User newContact;
    private String currentUserEmail;

    private ContactsAdapter contactsListAdapter;

    public ContactsFragment() {

    }
    public static ContactsFragment newInstance(User currentUser, String currentUserEmail, User newContact) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putSerializable(CURRENT_USER, currentUser);
        args.putSerializable(CURRENT_USER_EMAIL, currentUserEmail);
        args.putSerializable(NEW_CONTACT,newContact);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            currentUser = (User)getArguments().getSerializable(CURRENT_USER);
            newContact = (User)getArguments().getSerializable(NEW_CONTACT);
            currentUserEmail = getArguments().getString(CURRENT_USER_EMAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        emptyListText = (TextView)view.findViewById(R.id.empty_list_text);
        contactsList = (ListView)view.findViewById(R.id.students_list_view);
        emptyListText = (TextView)view.findViewById(R.id.empty_list_text);
        loadDetailsProgress = (ProgressBar)view.findViewById(R.id.loading_user_details_progress);

        contactsList.setOnScrollListener(new ListView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(contactsList.getChildAt(0).getTop() == 0)
                    MainActivity.getFab().show();
                else
                    MainActivity.getFab().hide();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        //Get current user type for setting correct views texts(Teacher/Student)
        final String currentUserEmailKey = DatabaseHandler.emailToValidKey(currentUserEmail);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserEmailKey).child("type");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserType type = dataSnapshot.getValue(UserType.class);
                if(type.equals(UserType.STUDENT)) {
                    emptyListText.setText(R.string.teachers_empty_list);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        loadContactsFromDatabase();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void loadContactsFromDatabase(){
        final String currentUserEmailKey = DatabaseHandler.emailToValidKey(currentUserEmail);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Get current user details
                String currentUserGroupName = dataSnapshot.child("users").child(currentUserEmailKey).child("type").getValue(UserType.class).getTypeGroupName();
                String contactsGroupName = dataSnapshot.child("users").child(currentUserEmailKey).child("type").getValue(UserType.class).getContactsGroupName();
                currentUser = dataSnapshot.child(currentUserGroupName).child(currentUserEmailKey).getValue(User.class);

                final ArrayList<User> contacts = new ArrayList<User>();
                for(DataSnapshot contact : dataSnapshot.child(currentUserGroupName).child(currentUserEmailKey).child(contactsGroupName).getChildren()){
                    User newContact = contact.getValue(User.class);
                    if(newContact != null)
                        contacts.add(newContact);
                }
                loadDetailsProgress.setVisibility(View.GONE);
                if(!contacts.isEmpty()) {
                    contactsList.setVisibility(View.VISIBLE);
                    emptyListText.setVisibility(View.GONE);
                }else{
                    emptyListText.setVisibility(View.VISIBLE);
                    contactsList.setVisibility(View.GONE);
                }

                //Set contacts list view adapter and listeners
                currentUser.setContacts(contacts);
                contactsListAdapter = new ContactsAdapter(getActivity(),currentUser.getContacts());
                contactsListAdapter.notifyDataSetChanged();

                contactsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
                //Set on item click
                contactsList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                        final int checkedCount = contactsList.getCheckedItemCount();
                        mode.setTitle(checkedCount + " Selected");
                        contactsListAdapter.toggleSelection(position);
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
                                SparseBooleanArray selected = contactsListAdapter
                                        .getSelectedIds();
                                // Captures all selected ids with a loop
                                for (int i = (selected.size() - 1); i >= 0; i--) {
                                    if (selected.valueAt(i)) {
                                        User selecteditem = (User)contactsListAdapter
                                                .getItem(selected.keyAt(i));
                                        // Remove selected items following the ids
                                        contactsListAdapter.remove(selecteditem);
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
                        contactsListAdapter.removeSelection();
                    }

                });
                contactsList.setAdapter(contactsListAdapter);
                contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent profilePageIntent = new Intent(getActivity(), UserProfileActivity.class);
                        profilePageIntent.putExtra("user",contacts.get(position));
                        User temp = contacts.get(position);
                        startActivity(profilePageIntent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
