package com.example.offlinemassage.ui.adapter;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.offlinemassage.R;
import com.example.offlinemassage.model.Msg;
import java.util.ArrayList;
import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<BluetoothViewHolder> {


     List<Msg> MsgList;




    public MessageAdapter(){
        MsgList = new ArrayList<>();
    }





    @SuppressLint("NotifyDataSetChanged")
    public void addMessage(String text, boolean isMine){

        Msg msg = new Msg(text, isMine);
        MsgList.add(msg);
        notifyDataSetChanged();

    }




    @NonNull
    @Override
    public BluetoothViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_input, parent, false);

        return  new BluetoothViewHolder(view);
    }





    @Override
    public void onBindViewHolder(@NonNull BluetoothViewHolder holder, int position) {

        Msg msg = MsgList.get(position);
        holder.tv.setText(msg.getText());


        messageColor(holder , msg);


    }





    private void messageColor(BluetoothViewHolder holder, Msg msg) {

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tv.getLayoutParams();

        if (msg.isMine){
            holder.tv.setBackgroundResource(R.drawable.otu_bg);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        }else {
            holder.tv.setBackgroundResource(R.drawable.in_bg);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        }

        holder.tv.setLayoutParams(params);
    }




    @Override
    public int getItemCount() {
        return MsgList.size();
    }


}
