package com.example.offlinemassage.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.offlinemassage.R;

public class BluetoothViewHolder extends RecyclerView.ViewHolder{


        AppCompatTextView tv;


        public BluetoothViewHolder(@NonNull View itemView) {
            super(itemView);

            tv = itemView.findViewById(R.id.text_input);
        }
    }

