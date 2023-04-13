package com.example.chattyparty.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.chattyparty.databinding.ItemContainerUserBinding;
import com.example.chattyparty.listeners.UserListener;
import com.example.chattyparty.model.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{
    private final List<User> users;
    private final UserListener userListener;
    public UsersAdapter(List<User> users,UserListener userListener) {
        this.users = users;
        this.userListener =userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       ItemContainerUserBinding itemContainerUserBinding= ItemContainerUserBinding.inflate(
               LayoutInflater.from(parent.getContext()),parent,false
       );
        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {

        return users.size();
    }




    class UserViewHolder extends RecyclerView.ViewHolder{
        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding =itemContainerUserBinding;
        }

        void setUserData(User user){
            binding.textName.setText(user.username);
            binding.textEmail.setText(user.email);
            Glide.with(binding.getRoot()).load(Uri.parse(user.avt)).into(binding.imageProfile);
            binding.imageProfile.setImageBitmap(getUserImage(user.avt));
            binding.getRoot().setOnClickListener(
                    v -> userListener.onUserClicked(user)
            );

        }
    }
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

}
