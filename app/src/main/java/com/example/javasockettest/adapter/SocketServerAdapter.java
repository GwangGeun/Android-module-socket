package com.example.javasockettest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.javasockettest.R;
import com.example.javasockettest.item.SocketServerItem;

import java.util.ArrayList;

public class SocketServerAdapter extends RecyclerView.Adapter<SocketServerAdapter.SocketServerViewHolder> {

    private ArrayList<SocketServerItem> items;
    private String itemId;
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
    public SocketServerAdapter(ArrayList<SocketServerItem> items){
        this.items = items;
    }

    // viewType 형태의 아이템 뷰를 위한 뷰홀더 객체 생성 : 필수 구현
    @NonNull
    @Override
    public SocketServerAdapter.SocketServerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_socket_server, parent, false);
        SocketServerViewHolder vh = new SocketServerViewHolder(view) ;

        return vh;
    }

    // position 에 해당하는 데이터를 뷰홀더의 아이템 뷰에 표시 : 필수 구현
    @Override
    public void onBindViewHolder(@NonNull final SocketServerAdapter.SocketServerViewHolder holder, final int pos) {
        itemId = items.get(pos).getSocketId();
        itemContent = items.get(pos).getSocketContent();
        holder.socketServerCellId.setText(itemId);
        holder.socketServerCellContent.setText(itemContent);

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
    public class SocketServerViewHolder extends  RecyclerView.ViewHolder{

        TextView socketServerCellId;
        TextView socketServerCellContent;

        // 생성자 : SocketServerItem 들의 view 지정
        public SocketServerViewHolder(@NonNull View itemView) {
            super(itemView);
            socketServerCellId = itemView.findViewById(R.id.item_socket_server_cell_id);
            socketServerCellContent = itemView.findViewById(R.id.item_socket_server_cell_content);
        }
    }

}
