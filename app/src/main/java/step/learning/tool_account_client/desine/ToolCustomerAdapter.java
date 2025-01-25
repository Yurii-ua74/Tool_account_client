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

public class ToolCustomerAdapter extends RecyclerView.Adapter<ToolCustomerAdapter.ToolViewHolder> {
    public List<ToolModel> toolList;

    public ToolCustomerAdapter(List<ToolModel> toolList) {
        this.toolList = toolList;
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tool_item, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        ToolModel tool = toolList.get(position);
        holder.toolId.setText(tool.getId());
        holder.toolName.setText(tool.getName());
        holder.toolModel.setText(tool.getModel());
        holder.toolNumber.setText(tool.getNumber());
        holder.toolOwner.setText(tool.getFirstname() + " " + tool.getLastname());
    }

    @Override
    public int getItemCount() {
        return toolList.size();
    }

    static class ToolViewHolder extends RecyclerView.ViewHolder {
        TextView toolId, toolName, toolModel, toolNumber, toolOwner;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            toolId = itemView.findViewById(R.id.toolId);
            toolName = itemView.findViewById(R.id.toolName);
            toolModel = itemView.findViewById(R.id.toolModel);
            toolNumber = itemView.findViewById(R.id.toolNumber);
            toolOwner = itemView.findViewById(R.id.toolOwner);
        }
    }

}
