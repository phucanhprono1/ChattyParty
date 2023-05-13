package com.example.chattyparty;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.chattyparty.data.FriendDB;
import com.example.chattyparty.data.GroupDB;
import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.model.Group;
import com.example.chattyparty.model.ListFriend;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class GroupFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView recyclerListGroups;
    public FragGroupClickFloatButton onClickFloatButton;
    private ArrayList<Group> listGroup;
    private ListGroupsAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static final int CONTEXT_MENU_DELETE = 1;
    public static final int CONTEXT_MENU_EDIT = 2;
    public static final int CONTEXT_MENU_LEAVE = 3;
    public static final int REQUEST_EDIT_GROUP = 0;
    public static final String CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos";
    public FriendDB friendDB;
    public GroupDB groupDB;



    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_group, container, false);
        friendDB = new FriendDB(getContext());
        groupDB = new GroupDB(getContext());
        listGroup = groupDB.getListGroups();
        recyclerListGroups = (RecyclerView) layout.findViewById(R.id.recycleListGroup);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerListGroups.setLayoutManager(layoutManager);
        adapter = new ListGroupsAdapter(getContext(), listGroup);
        recyclerListGroups.setAdapter(adapter);
        onClickFloatButton = new FragGroupClickFloatButton();


        if(listGroup.size() == 0){
            //Ket noi server hien thi group
            mSwipeRefreshLayout.setRefreshing(true);
            getListGroup();
        }
        return layout;
    }

    private void getListGroup(){
        FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("users/"+ StaticConfig.UID+"/group").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null) {
                    HashMap mapListGroup = (HashMap) dataSnapshot.getValue();
                    Iterator iterator = mapListGroup.keySet().iterator();
                    while (iterator.hasNext()){

                        String idGroup = (String) mapListGroup.get(iterator.next().toString());
                        Group newGroup = new Group();
                        newGroup.id = idGroup;
                        listGroup.add(newGroup);
                    }
                    getGroupInfo(0);
                }else{
                    mSwipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_EDIT_GROUP && resultCode == Activity.RESULT_OK) {
            listGroup.clear();
            ListGroupsAdapter.listFriend = null;
            groupDB.dropDB();
            getListGroup();
        }
    }

    private void getGroupInfo(final int indexGroup){
        if(indexGroup == listGroup.size()){
            adapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }else {
            FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("group/"+listGroup.get(indexGroup).id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue() != null){
                        HashMap mapGroup = (HashMap) dataSnapshot.getValue();
                        ArrayList<String> member = (ArrayList<String>) mapGroup.get("member");
                        HashMap mapGroupInfo = (HashMap) mapGroup.get("groupInfo");
                        for(String idMember: member){
                            listGroup.get(indexGroup).member.add(idMember);
                        }
                        listGroup.get(indexGroup).groupInfo.put("name", (String) mapGroupInfo.get("name"));
                        listGroup.get(indexGroup).groupInfo.put("admin", (String) mapGroupInfo.get("admin"));
                    }
                    groupDB.addGroup(listGroup.get(indexGroup));
                    Log.d("GroupFragment", listGroup.get(indexGroup).id +": " + dataSnapshot.toString());
                    getGroupInfo(indexGroup +1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }

    @Override
    public void onRefresh() {
        listGroup.clear();
        ListGroupsAdapter.listFriend = null;
        groupDB.dropDB();
        adapter.notifyDataSetChanged();
        getListGroup();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case CONTEXT_MENU_DELETE:
                Intent i = item.getIntent();
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int posGroup = i.getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(posGroup).groupInfo.get("admin")).equals(StaticConfig.UID)) {
                    Group group = listGroup.get(posGroup);
                    listGroup.remove(posGroup);
                    if(group != null){
                        deleteGroup(group, 0);
                    }
                }else{
                    Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
                }
                break;
            case CONTEXT_MENU_EDIT:
                Intent i1 = item.getIntent();
                i1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int posGroup1 = i1.getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(posGroup1).groupInfo.get("admin")).equals(StaticConfig.UID)) {
                    Intent intent = new Intent(getContext(), AddGroupActivity.class);
                    intent.putExtra("groupId", listGroup.get(posGroup1).id);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(intent, REQUEST_EDIT_GROUP);
                }else{
                    Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
                }

                break;

            case CONTEXT_MENU_LEAVE:
                Intent intent = item.getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                int position = intent.getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
                if(((String)listGroup.get(position).groupInfo.get("admin")).equals(StaticConfig.UID)) {
                    Toast.makeText(getActivity(), "Admin cannot leave group", Toast.LENGTH_LONG).show();
                }else{
                    Group groupLeaving = listGroup.get(position);
                    leaveGroup(groupLeaving);
                }
                break;
        }

        return super.onContextItemSelected(item);
    }

    public void deleteGroup(final Group group, final int index){
        if(index == group.member.size()){
            FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("group/"+group.id).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            groupDB.deleteGroup(group.id);
                            listGroup.remove(group);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getContext(), "Deleted group", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
                    ;
        }else{
            FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("users/"+group.member.get(index)+"/group/"+group.id).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            deleteGroup(group, index + 1);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    })
            ;
        }

    }

    public void leaveGroup(final Group group){
        FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("group/"+group.id+"/member")
                .orderByValue().equalTo(StaticConfig.UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            Toast.makeText(getContext(), "You are not in this group", Toast.LENGTH_SHORT).show();
                        } else {
                            String memberIndex = "";
                            ArrayList<String> result = ((ArrayList<String>)dataSnapshot.getValue());
                            for(int i = 0; i < result.size(); i++){
                                if(result.get(i) != null){
                                    memberIndex = String.valueOf(i);
                                }
                            }

                            FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user").child(StaticConfig.UID)
                                    .child("group").child(group.id).removeValue();
                            FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("group/"+group.id+"/member")
                                    .child(memberIndex).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                           // waitingLeavingGroup.dismiss();

                                            listGroup.remove(group);
                                            adapter.notifyDataSetChanged();
                                            groupDB.deleteGroup(group.id);
//
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
//
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public class FragGroupClickFloatButton implements View.OnClickListener{

        Context context;
        public FragGroupClickFloatButton getInstance(Context context){
            this.context = context;
            return this;
        }

        @Override
        public void onClick(View view) {
            startActivity(new Intent(getContext(), AddGroupActivity.class));
        }
    }
}
class ListGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Group> listGroup;
    public static ListFriend listFriend = null;
    private Context context;
    public FriendDB friendDB;

    public ListGroupsAdapter(Context context,ArrayList<Group> listGroup){
        this.context = context;
        this.listGroup = listGroup;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_group, parent, false);
        return new ItemGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String groupName = listGroup.get(position).groupInfo.get("name");
        friendDB = new FriendDB(context);
        if(groupName != null && groupName.length() > 0) {
            ((ItemGroupViewHolder) holder).txtGroupName.setText(groupName);
            ((ItemGroupViewHolder) holder).iconGroup.setText((groupName.charAt(0) + "").toUpperCase());
        }
        ((ItemGroupViewHolder) holder).btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setTag(new Object[]{groupName, position});
                view.getParent().showContextMenuForChild(view);
            }
        });
        ((RelativeLayout)((ItemGroupViewHolder) holder).txtGroupName.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listFriend == null){
                    listFriend = friendDB.getListFriend();
                }
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, groupName);
                ArrayList<CharSequence> idFriend = new ArrayList<>();
                ChatActivity.bitmapAvataFriend = new HashMap<>();
                for(String id : listGroup.get(position).member) {
                    idFriend.add(id);
                    String avata = listFriend.getAvataById(id);
                    if(!avata.equals(StaticConfig.STR_DEFAULT_URI)) {

                        ChatActivity.bitmapAvataFriend.put(id, avata);
                    }else if(avata.equals(StaticConfig.STR_DEFAULT_URI)) {

                        ChatActivity.bitmapAvataFriend.put(id, StaticConfig.STR_DEFAULT_URI);
                    }else {
                        ChatActivity.bitmapAvataFriend.put(id, null);
                    }
                }
                intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, listGroup.get(position).id);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listGroup.size();
    }
}

class ItemGroupViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    public TextView iconGroup, txtGroupName;
    public ImageButton btnMore;
    public ItemGroupViewHolder(View itemView) {
        super(itemView);
        itemView.setOnCreateContextMenuListener(this);
        iconGroup = (TextView) itemView.findViewById(R.id.icon_group);
        txtGroupName = (TextView) itemView.findViewById(R.id.txtName);
        btnMore = (ImageButton) itemView.findViewById(R.id.btnMoreAction);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        menu.setHeaderTitle((String) ((Object[])btnMore.getTag())[0]);
        Intent data = new Intent();
        data.putExtra(GroupFragment.CONTEXT_MENU_KEY_INTENT_DATA_POS, (Integer) ((Object[])btnMore.getTag())[1]);
        menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_EDIT, Menu.NONE, "Edit group").setIntent(data);
        menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_DELETE, Menu.NONE, "Delete group").setIntent(data);
        menu.add(Menu.NONE, GroupFragment.CONTEXT_MENU_LEAVE, Menu.NONE, "Leave group").setIntent(data);
    }
}