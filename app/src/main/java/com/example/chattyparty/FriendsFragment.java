package com.example.chattyparty;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.chattyparty.data.FriendDB;
import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.model.Friend;
import com.example.chattyparty.model.FriendRequest;
import com.example.chattyparty.model.ListFriend;
import com.example.chattyparty.model.Message;
import com.example.chattyparty.service.ServiceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerListFrends;
    private ListFriendsAdapter adapter;
    public FragFriendClickFloatButton onClickFloatButton;
    private ListFriend dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private ArrayList<String> listFriendID1 = null;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CountDownTimer detectFriendOnline;
    public static int ACTION_START_CHAT = 1;
    FriendDB friendDB;
    Friend user;
    Friend currUser;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    boolean connected = ServiceUtils.isNetworkConnected(getContext());
    public static final String ACTION_DELETE_FRIEND = "com..DELETE_FRIEND";
    DatabaseReference userRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    DatabaseReference friendRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend");

    DatabaseReference messageRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("message");
    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests");
    private BroadcastReceiver deleteFriendReceiver;

    public FriendsFragment() {
        onClickFloatButton = new FragFriendClickFloatButton();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                ServiceUtils.updateFriendStatus(getContext(), dataListFriend);
                ServiceUtils.updateUserStatus(getContext());
            }

            @Override
            public void onFinish() {

            }
        };
        friendDB = new FriendDB(getContext());
        if(connected){
            if (dataListFriend == null) {
                dataListFriend = friendDB.getListFriend();
                if (dataListFriend.getListFriend().size() > 0) {
                    listFriendID = new ArrayList<>();

                    for (Friend friend : dataListFriend.getListFriend()) {
                        listFriendID.add(friend.id);

                    }
                    detectFriendOnline.start();
                }
            }
            if (StaticConfig.LIST_FRIEND_ID == null) {
                StaticConfig.LIST_FRIEND_ID = new ArrayList<>();
                getListFriendUId();

            }
        }

        View layout = inflater.inflate(R.layout.fragment_people, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrends = layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ListFriendsAdapter(getContext(), dataListFriend, this);
        recyclerListFrends.setAdapter(adapter);


        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                if(connected){
                    friendRef.child(StaticConfig.UID).child(idDeleted).removeValue();
                    friendRef.child(idDeleted).child(StaticConfig.UID).removeValue();
                    for (Friend friend : dataListFriend.getListFriend()) {
                        if (idDeleted.equals(friend.id)) {

                            dataListFriend.getListFriend().remove(friend);
                            friendDB.deleteFriend(idDeleted);
                            friendRequestRef.child(StaticConfig.UID).child(idDeleted).removeValue();
                            friendRequestRef.child(idDeleted).child(StaticConfig.UID).removeValue();

                            Query query1 = friendRef.child(idDeleted).orderByValue().equalTo(StaticConfig.UID);
                            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    for (DataSnapshot childSnapshot: snapshot.getChildren()) {
                                        childSnapshot.getRef().removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            break;

                        }
                    }
                    adapter.notifyDataSetChanged();
                }

            }
        };

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);
        Log.d(TAG, "onCreateView: " + StaticConfig.ID_FRIEND_REQ);




        return layout;
    }
    private void addFriend(final String idFriend, boolean isIdFriend) {
        if (idFriend != null) {
            if (isIdFriend) {
                friendRef.child(StaticConfig.UID).push().setValue(idFriend)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    addFriend(idFriend, false);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//
                            }
                        });
            } else {
                friendRef.child(idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    addFriend(null, false);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//
                            }
                        });
            }
        } else {
//
//                Toast.makeText(getContext(), "Add friend success", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getContext().unregisterReceiver(deleteFriendReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_START_CHAT == requestCode && data != null && ListFriendsAdapter.mapMark != null) {
            ListFriendsAdapter.mapMark.put(data.getStringExtra("idFriend"), false);
        }
    }

    @Override
    public void onRefresh() {
        if(connected){
            StaticConfig.LIST_FRIEND_ID.clear();

            dataListFriend.getListFriend().clear();
            adapter.notifyDataSetChanged();
            friendDB.dropDB();
            detectFriendOnline.cancel();

            getListFriendUId();
        }
        else {
            Toast.makeText(getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    String text;

    public class FragFriendClickFloatButton implements View.OnClickListener {
        Context context;

        public FragFriendClickFloatButton() {
        }

        public FragFriendClickFloatButton getInstance(Context context) {
            this.context = context;
            return this;
        }

        @Override
        public void onClick(final View view) {
//            Intent i = new Intent(getContext(),AddFriendActivity.class);
//            startActivity(i);
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT);

            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.post(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(context)
                            .setTitle("Nhập gmail")
                            .setView(input)
                            .setMessage("")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    text = input.getText().toString();
                                    findIDEmail(text);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                }
            });
        }

        /**
         * TIm id cua email tren server
         *
         * @param email
         */
        private void findIDEmail(String email) {


            userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //dialogWait.dismiss();
                    if (dataSnapshot.getValue() == null) {
                        Toast.makeText(getContext(), "email not found", Toast.LENGTH_SHORT);
                        //email not found
//
                    } else {
                        String id = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                        if (id.equals(StaticConfig.UID)) {
                            Toast.makeText(getContext(), "email not valid", Toast.LENGTH_SHORT);

                        } else {
                            HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue()).get(id);
                            StaticConfig.ID_FRIEND_REQ = id;
                            Log.d(TAG, "onDataChange: " + StaticConfig.ID_FRIEND_REQ);
                            user = new Friend();
                            user.name = (String) userMap.get("name");
                            user.email = (String) userMap.get("email");
                            user.avata = (String) userMap.get("avata");
                            user.id = id;
                            user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                            StaticConfig.FRIEND_REQUEST = user;
                            currUser = new Friend();
                            currUser.name = StaticConfig.NAME;
                            currUser.email = StaticConfig.EMAIL;
                            currUser.id = StaticConfig.UID;
                            currUser.avata = StaticConfig.AVATA;
                            currUser.idRoom= user.idRoom;
                            Log.d(TAG, "onDataChange: " + StaticConfig.FRIEND_REQUEST.toString());
                            checkBeforeAddFriend(id, user, currUser);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

        DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("friend_requests");

        /**
         * Lay danh sach friend cua một UID
         */
        private void checkBeforeAddFriend(final String idFriend, Friend userInfo,Friend currUser) {


            //Check xem da ton tai id trong danh sach id chua
            if (StaticConfig.LIST_FRIEND_ID!=null&&StaticConfig.LIST_FRIEND_ID.contains(idFriend)) {
                Toast.makeText(getContext(), "User " + userInfo.email + " has been friend", Toast.LENGTH_SHORT);

//
            } else {
                addFriendRequest(idFriend, userInfo,currUser);


            }
//


        }

        private void addFriendRequest(final String idFriend, Friend userInfo,Friend currUser) {
            if (idFriend != null) {

                String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                HashMap<String, Object> friendRequestMap = new HashMap<>();
                userRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.child("name").getValue().toString();
                        friendRequestMap.put("senderName", name);
                        friendRequestMap.put("senderImage", snapshot.child("avata").getValue().toString());
                        friendRequestMap.put("sender", currentUserID);

                        friendRequestMap.put("receiver", idFriend);
                        friendRequestMap.put("status", "pending");
                        friendRequestMap.put("id",currUser.id);
                        friendRequestMap.put("email",currUser.email);
                        friendRequestMap.put("avata",currUser.avata);
                        friendRequestMap.put("name",currUser.name);
                        friendRequestMap.put("idRoom",currUser.idRoom);

                        DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("friend_requests");
                        friendRequestRef.child(idFriend).child(currentUserID).setValue(friendRequestMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Toast.makeText(getContext(), "Friend request sent", Toast.LENGTH_SHORT).show();
//                                        } else {
//                                            Toast.makeText(getContext(), "Failed to send friend request", Toast.LENGTH_SHORT).show();
//                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Failed to send friend request", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        // Add a ValueEventListener to the friend request node

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled event
                    }
                });
            }
        }

        /**
         * Add friend
         *
         * @param idFriend
         */
        private void addFriend(final String idFriend, boolean isIdFriend) {
            if (idFriend != null) {
                if (isIdFriend) {
                    friendRef.child(StaticConfig.UID).push().setValue(idFriend)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        addFriend(idFriend, false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
//
                                }
                            });
                } else {
                    friendRef.child(idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        addFriend(null, false);
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
//
                                }
                            });
                }
            } else {
//
//                Toast.makeText(getContext(), "Add friend success", Toast.LENGTH_SHORT);
            }
        }
    }

    /**
     * Lay danh sach ban be tren server
     */
    private void getListFriendUId() {
        friendRef.child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        StaticConfig.LIST_FRIEND_ID.add(mapRecord.get(key).toString());
                    }
                    getAllFriendInfo(0);

                } else {
                    Toast.makeText(getContext(), "null", Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Truy cap bang user lay thong tin id nguoi dung
     */
    private void getAllFriendInfo(final int index) {
        if (index == StaticConfig.LIST_FRIEND_ID.size()) {
            //save list friend
            adapter.notifyDataSetChanged();

            mSwipeRefreshLayout.setRefreshing(false);
            detectFriendOnline.start();
        } else {
            final String id = StaticConfig.LIST_FRIEND_ID.get(index);
            userRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        dataListFriend.getListFriend().add(user);
                        friendDB.addFriend(user);

                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}

class ListFriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ListFriend listFriend;
    private final Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private final FriendsFragment fragment;
//    LovelyProgressDialog dialogWaitDeleting;


    public ListFriendsAdapter(Context context, ListFriend listFriend, FriendsFragment fragment) {
        this.listFriend = listFriend;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        this.fragment = fragment;
//        dialogWaitDeleting = new LovelyProgressDialog(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_friend, parent, false);
        return new ItemFriendViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final String name = listFriend.getListFriend().get(position).name;
        final String id = listFriend.getListFriend().get(position).id;
        final String idRoom = listFriend.getListFriend().get(position).idRoom;
        final String avata = listFriend.getListFriend().get(position).avata;
        ((ItemFriendViewHolder) holder).txtName.setText(name);
        boolean connected = ServiceUtils.isNetworkConnected(((ItemFriendViewHolder) holder).context);

        ((View) ((ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                        ((ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, name);
                        ArrayList<CharSequence> idFriend = new ArrayList<CharSequence>();
                        idFriend.add(id);
                        intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
                        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, idRoom);
                        intent.putExtra(StaticConfig.AVATA, avata);

                        ChatActivity.bitmapAvataFriend = new HashMap<>();
                        if (!avata.equals(StaticConfig.STR_DEFAULT_URI)) {
////                            byte[] decodedString = Base64.decode(avata, Base64.DEFAULT);
                            ChatActivity.bitmapAvataFriend.put(id, avata);
                        } else {
                            ChatActivity.bitmapAvataFriend.put(id, StaticConfig.AVATA);
                        }

                        mapMark.put(id, null);
                        fragment.startActivityForResult(intent, FriendsFragment.ACTION_START_CHAT);
                    }
                });

        //nhấn giữ để xóa bạn
        ((View) ((ItemFriendViewHolder) holder).txtName.getParent().getParent().getParent())
                .setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        String friendName = (String) ((ItemFriendViewHolder) holder).txtName.getText();
//                        final String idRemove = listFriend.getListFriend().get(position).id;
//                        deleteFriend(idRemove);
                        new AlertDialog.Builder(context)
                                .setTitle("Choose Action")
                                .setMessage("Do you want to delete " + friendName + " or view their wall?")
                                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        final String idFriendRemoval = listFriend.getListFriend().get(position).id;
                                        if(connected){
                                            deleteFriend(idFriendRemoval);
                                        }

                                    }
                                })
                                .setNegativeButton("View Wall", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        Intent intent = new Intent(context, FriendWallActivity.class);
                                        intent.putExtra("friendId", listFriend.getListFriend().get(position).id);
                                        context.startActivity(intent);
                                    }
                                }).show();

                        return true;
                    }
                });


        if (listFriend.getListFriend().get(position).message.text.length() > 0 && connected) {
            ((ItemFriendViewHolder) holder).txtMessage.setVisibility(View.VISIBLE);
            ((ItemFriendViewHolder) holder).txtTime.setVisibility(View.VISIBLE);
            if (!listFriend.getListFriend().get(position).message.text.startsWith(id)) {
                ((ItemFriendViewHolder) holder).txtMessage.setText(listFriend.getListFriend().get(position).message.text);
                ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT);
                ((ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT);
            } else {
                ((ItemFriendViewHolder) holder).txtMessage.setText(listFriend.getListFriend().get(position).message.text.substring((id + "").length()));
                ((ItemFriendViewHolder) holder).txtMessage.setTypeface(Typeface.DEFAULT_BOLD);
                ((ItemFriendViewHolder) holder).txtName.setTypeface(Typeface.DEFAULT_BOLD);
            }
            String time = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(listFriend.getListFriend().get(position).message.timestamp));
            String today = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));
            if (today.equals(time)) {
                ((ItemFriendViewHolder) holder).txtTime.setText(new SimpleDateFormat("HH:mm").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
            } else {
                ((ItemFriendViewHolder) holder).txtTime.setText(new SimpleDateFormat("MMM d").format(new Date(listFriend.getListFriend().get(position).message.timestamp)));
            }
        } else {
            ((ItemFriendViewHolder) holder).txtMessage.setVisibility(View.GONE);
            ((ItemFriendViewHolder) holder).txtTime.setVisibility(View.GONE);
            if (mapQuery.get(id) == null && mapChildListener.get(id) == null) {
                mapQuery.put(id, FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("message").child(idRoom).limitToLast(1));
                mapChildListener.put(id, new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                        if (mapMark.get(id) != null) {
                            if (!mapMark.get(id)) {
                                listFriend.getListFriend().get(position).message.text = id + mapMessage.get("text");
                            } else {
                                listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                            }
                            notifyDataSetChanged();
                            mapMark.put(id, false);
                        } else {
                            listFriend.getListFriend().get(position).message.text = (String) mapMessage.get("text");
                            notifyDataSetChanged();
                        }
                        listFriend.getListFriend().get(position).message.timestamp = (long) mapMessage.get("timestamp");
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                mapMark.put(id, true);
            } else {
                mapQuery.get(id).removeEventListener(mapChildListener.get(id));
                mapQuery.get(id).addChildEventListener(mapChildListener.get(id));
                mapMark.put(id, true);
            }
        }
        if (listFriend.getListFriend().get(position).avata.equals(StaticConfig.AVATA)) {
            Glide.with(holder.itemView).load(StaticConfig.AVATA).into(((ItemFriendViewHolder) holder).avata);
        } else {

            Glide.with(holder.itemView).load(Uri.parse(listFriend.getListFriend().get(position).avata)).into(((ItemFriendViewHolder) holder).avata);

        }


        if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null && connected) {
            mapQueryOnline.put(id, FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users").child(id).child("status"));
            mapChildListenerOnline.put(id, new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                        Log.d("FriendsFragment add " + id, (boolean) dataSnapshot.getValue() + "");
                        listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                        Log.d("FriendsFragment change " + id, (boolean) dataSnapshot.getValue() + "");
                        listFriend.getListFriend().get(position).status.isOnline = (boolean) dataSnapshot.getValue();
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mapQueryOnline.get(id).addChildEventListener(mapChildListenerOnline.get(id));
        }

        if (listFriend.getListFriend().get(position).status.isOnline) {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(10);
        } else {
            ((ItemFriendViewHolder) holder).avata.setBorderWidth(0);
        }
    }

    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;
    }

    /**
     * Delete friend
     *
     * @param idFriend
     */
    private void deleteFriend(final String idFriend) {
        if (idFriend != null && fragment.connected) {
            FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend").child(StaticConfig.UID)
                    .orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() == null) {

                            } else {
                                String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                                FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend")
                                        .child(StaticConfig.UID).child(idRemoval).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(context, "Deleted friend", Toast.LENGTH_SHORT);

                                                Intent intentDeleted = new Intent(FriendsFragment.ACTION_DELETE_FRIEND);
                                                intentDeleted.putExtra("idFriend", idFriend);
                                                context.sendBroadcast(intentDeleted);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });
                                FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests").child(StaticConfig.UID).child(idRemoval).removeValue();
                                FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests").child(idRemoval).child(StaticConfig.UID).removeValue();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            Toast.makeText(context, "Error occurred during deleting friend", Toast.LENGTH_SHORT);

        }
    }

    class ItemFriendViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView avata;
        public TextView txtName, txtTime, txtMessage;
        private final Context context;

        ItemFriendViewHolder(Context context, View itemView) {
            super(itemView);
            avata = itemView.findViewById(R.id.icon_avata);
            txtName = itemView.findViewById(R.id.txtName);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            this.context = context;
        }
    }
}

