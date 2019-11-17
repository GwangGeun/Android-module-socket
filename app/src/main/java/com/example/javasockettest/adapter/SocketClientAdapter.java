package com.example.javasockettest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javasockettest.R;
import com.example.javasockettest.item.SocketClientItem;

import java.util.ArrayList;

public class SocketClientAdapter extends RecyclerView.Adapter<SocketClientAdapter.SocketClientViewHolder> {

    private ArrayList<SocketClientItem> items;
    private String itemTime;
    private String itemContent;

    // 아이템 클릭 이벤트를 액티비티에서 처리하기 위한 로직
    public interface OnItemClickListener{
        void onItemClick(View v, int pos);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    // 생성자 : 필수 구현
    public SocketClientAdapter(ArrayList<SocketClientItem> items){
        this.items = items;
    }

    // viewType 형태의 아이템 뷰를 위한 뷰홀더 객체 생성 : 필수 구현
    @NonNull
    @Override
    public SocketClientAdapter.SocketClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_socket_client, parent, false);
        SocketClientAdapter.SocketClientViewHolder vh = new SocketClientAdapter.SocketClientViewHolder(view) ;
        return vh;
    }

    // position 에 해당하는 데이터를 뷰홀더의 아이템 뷰에 표시 : 필수 구현
    @Override
    public void onBindViewHolder(@NonNull SocketClientAdapter.SocketClientViewHolder holder, final int pos) {
        itemTime = items.get(pos).getChatTime();
        itemContent = items.get(pos).getChatContent();
        holder.chatTime.setText(itemTime);
        holder.chatContent.setText(itemContent);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mListener != null){
                    mListener.onItemClick(v, pos);
                }

            }
        });
    }

    // 	전체 아이템 갯수를 반환 : 필수 구현
    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder : 필수 구현
    public class SocketClientViewHolder extends  RecyclerView.ViewHolder{

        TextView chatTime;
        TextView chatContent;

        // 생성자 : SocketClientItem 들의 view 지정
        public SocketClientViewHolder(@NonNull View itemView) {
            super(itemView);
            chatTime = itemView.findViewById(R.id.item_socket_client_chat_time);
            chatContent = itemView.findViewById(R.id.item_socket_client_chat_content);
        }
    }

}
