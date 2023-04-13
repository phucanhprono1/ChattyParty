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
import com.example.chattyparty.databinding.ItemContainerRecentConversionBinding;
import com.example.chattyparty.listeners.ConversionListener;
import com.example.chattyparty.model.ChatMessage;
import com.example.chattyparty.model.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>{
    private final List<ChatMessage> chatMessages;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.conversionListener =conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),parent,false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {

        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;
        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding =itemContainerRecentConversionBinding;
        }
        void setData(ChatMessage chatMessage){
            Glide.with(binding.getRoot()).load(Uri.parse(chatMessage.conversionImage)).into(binding.imageProfile);
            //binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v ->{
                User user =new User();
                user.userId=chatMessage.conversionId;
                user.username=chatMessage.conversionName;
                user.avt=chatMessage.conversionImage;
                conversionListener.onConversionClicked(user);
            });
        }
    }

//    private Bitmap getConversionImage(String encodedImage){
//        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
//        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
//    }
    private Bitmap getConversionImage(String encodedImage){
        if (encodedImage == null) {
            // Xử lý trường hợp encodedImage là null
            return null;
        }
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
