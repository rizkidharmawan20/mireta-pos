package com.boardinglabs.mireta.standalone.component.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boardinglabs.mireta.standalone.R;
import com.boardinglabs.mireta.standalone.component.fontview.RobotoBoldTextView;
import com.boardinglabs.mireta.standalone.component.network.ApiLocal;
import com.boardinglabs.mireta.standalone.component.network.entities.Expenditure.ExpenditureResponse;
import com.boardinglabs.mireta.standalone.component.network.response.ApiResponse;
import com.boardinglabs.mireta.standalone.component.util.Loading;
import com.boardinglabs.mireta.standalone.component.util.MethodUtil;
import com.boardinglabs.mireta.standalone.component.util.PreferenceManager;
import com.boardinglabs.mireta.standalone.modul.expenditure.ExpenditureActivity;
import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenditureAdapter extends RecyclerView.Adapter<ExpenditureAdapter.ViewHolder> {
    private List<ExpenditureResponse> expenditureResponses;
    private Context context;
    private Dialog dialog;

    public ExpenditureAdapter(List<ExpenditureResponse> ExpenditureResponses, Context context){
        this.expenditureResponses = ExpenditureResponses;
        this.context = context;
    }

    @NonNull
    @Override
    public ExpenditureAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_expenditure, parent, false);

        return new ExpenditureAdapter.ViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(@NonNull ExpenditureAdapter.ViewHolder holder, int position){
        try {
            final ExpenditureResponse expenditureResponse = expenditureResponses.get(position);
            final String id = String.valueOf(expenditureResponse.getId());
            final String name = expenditureResponse.getName();
            final String description = expenditureResponse.getDescription();
            final String nominal = expenditureResponse.getNominal();
            final String image = expenditureResponse.getImage().replace("index.php/", "");
            final String locationName = expenditureResponse.getLocation().getName();
            final String order_date = expenditureResponse.getCreatedAt();

            Date d = null;
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                d = sdf.parse(order_date);
            } catch (ParseException ex) {
                Log.v("Exception", ex.getLocalizedMessage());
            }

            sdf.applyPattern("dd MMM yyyy");
            String orderDate = sdf.format(d);
            sdf.applyPattern("HH:mm");
            String orderTime = sdf.format(d);

            holder.tvName.setText(name);
            holder.tvOrderDate.setText(orderDate + " " + orderTime);
            holder.tvAmount.setText("Rp. " + MethodUtil.toCurrencyFormat(nominal));

            Glide.with(context)
                    .load(image)
                    .placeholder(R.drawable.iconpenjualan)
                    .into(holder.icon_transaction);
            holder.tvDescription.setVisibility(View.GONE);
            holder.icon_transaction.setOnClickListener(v -> {
                showImage(R.layout.dialog_image, context);
                ImageView btnClose = dialog.findViewById(R.id.btnClose);
                ImageView imgExpenditure = dialog.findViewById(R.id.imgExpenditure);
                btnClose.setOnClickListener(v1 -> dialog.dismiss());
                Glide.with(context)
                        .load(image)
                        .placeholder(R.drawable.iconpenjualan)
                        .into(imgExpenditure);
            });

            Date finalD = d;
//        holder.layoutTransaction.setOnClickListener(view -> {
//            Intent intent = new Intent(context, DetailTransactionActivity.class);
//            intent.putExtra("id", id);
//            intent.putExtra("order_date", orderDate);
//            intent.putExtra("order_time", orderTime);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//        });

            holder.imgDelete.setOnClickListener(v -> {
                showDialog(R.layout.layout_delete_confirmation, context);
                Button btnDelete = dialog.findViewById(R.id.btnDelete);
                ImageView btnClose = dialog.findViewById(R.id.btnClose);

                btnDelete.setOnClickListener(v1 -> deleteExpenditure(id, position, dialog));
                btnClose.setOnClickListener(v1 -> dialog.dismiss());
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void deleteExpenditure(String id, int position, Dialog dialog) {
        Loading.show(context);
        ApiLocal.apiInterface().deleteExpenditure(id, "delete", "Bearer " + PreferenceManager.getSessionToken()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                try {
                    dialog.dismiss();
                    Loading.hide(context);
                    if (response.isSuccessful()){
                        Toast.makeText(context, "Berhasil menghapus pengeluaran", Toast.LENGTH_SHORT).show();
                        expenditureResponses.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, expenditureResponses.size());
                    } else {
                        Toast.makeText(context, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Loading.hide(context);
                dialog.dismiss();
                t.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount(){
        return expenditureResponses.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription;
        RobotoBoldTextView tvName;
        RobotoBoldTextView tvAmount;
        TextView tvOrderDate;
        LinearLayout layoutTransaction;
        FrameLayout layoutDelete;
        ImageView icon_transaction, imgDelete;

        ViewHolder(View v){
            super(v);

            tvDescription = v.findViewById(R.id.status_transaction);
            tvName = v.findViewById(R.id.transaction_id);
            tvAmount = v.findViewById(R.id.amount_transaction);
            tvOrderDate = v.findViewById(R.id.time_transaction);
            layoutTransaction = v.findViewById(R.id.container_transaction);
            layoutDelete = v.findViewById(R.id.user_feed_row_bottom);
            icon_transaction = v.findViewById(R.id.icon_transaction);
            imgDelete = v.findViewById(R.id.imgDelete);
        }
    }

    private void showDialog(int layout, Context context) {
        dialog = new Dialog(Objects.requireNonNull(context));
        //set content
        dialog.setContentView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void showImage(int layout, Context context) {
        dialog = new Dialog(Objects.requireNonNull(context));
        //set content
        dialog.setContentView(layout);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public void updateData(List<ExpenditureResponse> newUser){
        expenditureResponses = new ArrayList<>();
        expenditureResponses.addAll(newUser);
        notifyDataSetChanged();
    }
}
