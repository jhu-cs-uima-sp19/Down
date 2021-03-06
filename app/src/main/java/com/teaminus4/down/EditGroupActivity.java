package com.teaminus4.down;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import static com.teaminus4.down.CreateGroupActivity.avatarAddList;
import static com.teaminus4.down.CreateGroupActivity.emailAddList;
import static com.teaminus4.down.CreateGroupActivity.nameAddList;
import static com.teaminus4.down.CreateGroupActivity.recyclerView2;
import static com.teaminus4.down.CreateGroupActivity.selUIDList;

public class EditGroupActivity extends AppCompatActivity {
    EditText search_edit_text;
    EditText create_group_name;
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    DatabaseReference userFriends;
    FirebaseUser firebaseUser;

    ArrayList<GroupElement> displayFriends;

    ArrayList<String> nameList;
    ArrayList<String> emailList;
    ArrayList<Integer> avatarList;
    ArrayList<String> UIDList;


    static ArrayList<Boolean> selList;

    ArrayList<String> friendList;
    String groupName;
    SearchAdapterNewGroups searchAdapterNewGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("Edit Your Group");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        search_edit_text = (EditText) findViewById(R.id.search_edit_text);
        create_group_name = (EditText) findViewById(R.id.create_group_name);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView2 = (RecyclerView) findViewById(R.id.recyclerView2);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String uid = firebaseUser.getUid();

        userFriends = FirebaseDatabase.getInstance().getReference().child("users").child(uid);


        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        //LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView2.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));


        //Creates a array list for each node
        nameList = new ArrayList<>();
        emailList = new ArrayList<>();
        avatarList = new ArrayList<>();
        UIDList = new ArrayList<>();
        selList = new ArrayList<>();

        displayFriends = new ArrayList<>();

        nameAddList = new ArrayList<>();
        emailAddList = new ArrayList<>();
        avatarAddList = new ArrayList<>();
        friendList = new ArrayList<>();
        selUIDList = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        nameAddList = b.getStringArrayList("nameList");
        emailAddList = b.getStringArrayList("emailList");
        avatarAddList = b.getIntegerArrayList("avatarList");
        selUIDList = b.getStringArrayList("uidList");
        groupName = b.getString("groupName");
        create_group_name.setText(groupName);

        FloatingActionButton fab = this.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_check);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userFriends.child("groups").child(groupName).removeValue();
                groupName = create_group_name.getText().toString();
                if(groupName.isEmpty()){
                    create_group_name.setError("Please enter a group name.");
                    return;
                }

                if(selUIDList.size() < 2){
                    search_edit_text.setError("Please select at least 2 friends.");
                    return;
                }

                for (int i = 0; i < selUIDList.size(); i++) {
                    userFriends.child("groups").child(groupName).child(selUIDList.get(i)).setValue(nameAddList.get(i));
                }
                Intent i = new Intent(EditGroupActivity.this, GroupClickedActivity.class);
                i.putExtra("GROUP_NAME", groupName);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putStringArrayListExtra("GROUP_UIDS", selUIDList);
                startActivity(i);

            }
        });

        userFriends.child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    friendList.add(snapshot.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //Sets adapter to display friends options
        setAdapter("");

        //Method to change what friends are displayed with change in text
        search_edit_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    setAdapter(s.toString());
                } else {
                    /*
                     * Clear the list when editText is empty
                     * */
                    nameList.clear();
                    emailList.clear();
                    avatarList.clear();
                    UIDList.clear();
                    selList.clear();
                    recyclerView.removeAllViews();
                    setAdapter("");
                }
            }
        });
    }

    private void setAdapter(final String searchedString) {
        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                 * Clear the list for every new search
                 * */
                nameList.clear();
                emailList.clear();
                avatarList.clear();
                UIDList.clear();
                selList.clear();
                recyclerView.removeAllViews();

                int counter = 0;

                if (searchedString.length() == 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String UID = snapshot.getKey();
                        Integer avatarIndex = snapshot.child("avatar").getValue(Integer.class);
                        if (friendList.contains(UID)) {
                            nameList.add(name);
                            emailList.add(email);
                            UIDList.add(UID);
                            avatarList.add(avatarIndex);
                            selList.add(false);
                            counter++;
                        }
                        if (counter == 15)
                            break;
                    }

                } else {

                    /*
                     * Search all users for matching searched string
                     * */
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        /*
                        if (snapshot.child("friends").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            continue;
                        }
                        */

                        String name = snapshot.child("name").getValue(String.class);
                        String email = snapshot.child("email").getValue(String.class);
                        String UID = snapshot.getKey();
                        Integer avatarIndex = snapshot.child("avatar").getValue(Integer.class);
                            if (name.toLowerCase().contains(searchedString.toLowerCase())) {
                                if (friendList.contains(UID)) {
                                    nameList.add(name);
                                    emailList.add(email);
                                    UIDList.add(UID);
                                    avatarList.add(avatarIndex);
                                    selList.add(false);
                                    counter++;
                                }
                            } else if (email.toLowerCase().contains(searchedString.toLowerCase())) {
                                if (friendList.contains(UID)) {
                                    nameList.add(name);
                                    emailList.add(email);
                                    UIDList.add(UID);
                                    avatarList.add(avatarIndex);
                                    selList.add(false);
                                    counter++;
                                }
                            }


                        /*
                         * Get maximum of 15 searched results only
                         * */
                        if (counter == 15)
                            break;
                    }
                }


                searchAdapterNewGroups = new SearchAdapterNewGroups(EditGroupActivity.this, nameList, emailList, avatarList, UIDList);
                //inGroupAdapterNewGroups = new InGroupAdapterNewGroups(CreateGroupActivity.this, nameAddList, emailAddList, avatarAddList, selUIDList);


                //SearchAdapter(AddFriendActivity.this, nameList, emailList);
                recyclerView.setAdapter(searchAdapterNewGroups);
                //recyclerView2.setAdapter(inGroupAdapterNewGroups);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public class GroupElement
    {
        // Instance Variables
        String name;
        String UID;
        String email;
        int avatarIndex;

        // Constructor Declaration of Class
        public GroupElement(String name, String UID, String email,
                            int avatarIndex)
        {
            this.name = name;
            this.UID = UID;
            this.email = email;
            this.avatarIndex = avatarIndex;
        }

        // method 1
        public String getName()
        {
            return name;
        }

        // method 2
        public String getUID()
        {
            return UID;
        }

        // method 3
        public int getAvatarIndex()
        {
            return avatarIndex;
        }

        public String getEmail() { return email; }
    }
}