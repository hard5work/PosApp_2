package bonfire.app.pos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pos.R;

import java.util.List;

import bonfire.app.pos.modules.DataModule;

public class ComboItemViewer extends RecyclerView.Adapter<ComboItemViewer.ComboItemHolder> {
    Context context;
    List<DataModule> dataModules;

    public ComboItemViewer(Context context, List<DataModule> dataModules) {
        this.context = context;
        this.dataModules = dataModules;
    }

    @NonNull
    @Override
    public ComboItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ComboItemHolder(LayoutInflater.from(context).inflate(R.layout.card_combo_item_name, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ComboItemHolder holder, int position) {
        holder.itemname.setText(dataModules.get(holder.getAdapterPosition()).getCombo_product_name());

    }

    @Override
    public int getItemCount() {
        return dataModules.size();
    }


    public class ComboItemHolder extends RecyclerView.ViewHolder{

        TextView itemname;
        public ComboItemHolder(@NonNull View itemView) {
            super(itemView);
            itemname = itemView.findViewById(R.id.itemnamecombo);
        }
    }
}
