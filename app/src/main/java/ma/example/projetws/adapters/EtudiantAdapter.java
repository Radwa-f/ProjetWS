package ma.example.projetws.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ma.example.projetws.ListActivity;
import ma.example.projetws.R;
import ma.example.projetws.beans.Etudiant;
import com.bumptech.glide.Glide;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<Etudiant> etudiantList;
    private List<Etudiant> etudiantsFilter;
    private NewFilter mfilter;

    public EtudiantAdapter(Context context, List<Etudiant> etudiantList) {
        this.context = context;
        this.etudiantList = etudiantList;
        etudiantsFilter = new ArrayList<>(etudiantList);
        mfilter = new NewFilter(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Etudiant etudiant = etudiantList.get(position);
        holder.fullname.setText(etudiant.getNom() + " " + etudiant.getPrenom());
        holder.ville.setText(etudiant.getVille());
        holder.sexe.setText(etudiant.getSexe());

        if (etudiant.getPhoto() != null && !etudiant.getPhoto().isEmpty()) {

            byte[] decodedString = Base64.decode(etudiant.getPhoto(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            Glide.with(context)
                    .load(bitmap)
                    .placeholder(R.drawable.star)
                    .error(R.drawable.error)
                    .into(holder.img);
        } else {

            holder.img.setImageResource(R.drawable.star);
        }
        holder.itemView.setOnClickListener(v -> {
            ((ListActivity) context).showEditDialog(etudiant);
        });
    }


    @Override
    public int getItemCount() {
        return etudiantList.size();
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fullname, ville, sexe;
        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.fullname);
            ville = itemView.findViewById(R.id.ville);
            sexe = itemView.findViewById(R.id.sexe);
            img = itemView.findViewById(R.id.img);
        }
    }
    public class NewFilter extends Filter {
        private final RecyclerView.Adapter myAdapter;

        public NewFilter(RecyclerView.Adapter myAdapter) {
            super();
            this.myAdapter = myAdapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            etudiantsFilter.clear();
            final FilterResults results = new FilterResults();
            if (charSequence.length() == 0) {
                etudiantsFilter.addAll(etudiantList);
            } else {
                final String filterPattern = charSequence.toString().toLowerCase().trim();
                for (Etudiant p : etudiantList) {
                    if (p.getNom().toLowerCase().startsWith(filterPattern)) {
                        etudiantsFilter.add(p);
                    }
                }
            }
            results.values = etudiantsFilter;
            results.count = etudiantsFilter.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            etudiantsFilter = (List<Etudiant>) filterResults.values;
            this.myAdapter.notifyDataSetChanged();
        }
    }


}