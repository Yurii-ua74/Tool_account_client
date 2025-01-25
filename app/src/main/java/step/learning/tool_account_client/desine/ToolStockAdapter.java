package step.learning.tool_account_client.desine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import step.learning.tool_account_client.R;
import step.learning.tool_account_client.models.ToolModel;

public class ToolStockAdapter extends RecyclerView.Adapter<ToolStockAdapter.ToolViewHolder> {
    private final List<ToolModel> toolStockList;

    public ToolStockAdapter(List<ToolModel> toolList) {
        this.toolStockList = toolList;
    }

    @NonNull
    @Override
    public ToolStockAdapter.ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tool_item_stock, parent, false);
        return new ToolStockAdapter.ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolStockAdapter.ToolViewHolder holder, int position) {
        ToolModel tool = toolStockList.get(position);
        holder.toolStockName.setText(tool.getName());
        holder.toolStockModel.setText(tool.getModel());
        holder.toolStockNumber.setText(tool.getNumber());
        holder.toolStockOwner.setText(tool.getFirstname() + " " + tool.getLastname());
    }

    @Override
    public int getItemCount() {
        return toolStockList.size();
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {
        TextView toolStockName, toolStockModel, toolStockNumber, toolStockOwner;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            toolStockName = itemView.findViewById(R.id.toolStockName);
            toolStockModel = itemView.findViewById(R.id.toolStockModel);
            toolStockNumber = itemView.findViewById(R.id.toolStockNumber);
            toolStockOwner = itemView.findViewById(R.id.toolStockOwner);
        }
    }
}
