package ma.example.projetws;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ma.example.projetws.adapters.EtudiantAdapter;
import ma.example.projetws.beans.Etudiant;

public class ListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EtudiantAdapter etudiantAdapter;
    private List<Etudiant> etudiantList;
    private List<Etudiant> filteredEtudiantList;
    private RequestQueue requestQueue;
    private static final String TAG = "ListActivity";
    String fetchUrl = "http://10.0.2.2/php_volley/ws/loadEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Recherche");

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        etudiantList = new ArrayList<>();
        filteredEtudiantList = new ArrayList<>();

        etudiantAdapter = new EtudiantAdapter(this, filteredEtudiantList);
        recyclerView.setAdapter(etudiantAdapter);

        requestQueue = Volley.newRequestQueue(this);
        fetchEtudiants();


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void fetchEtudiants() {
        StringRequest request = new StringRequest(Request.Method.GET, fetchUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: " + response);
                        if (response.startsWith("[")) {
                            Type type = new TypeToken<Collection<Etudiant>>() {}.getType();
                            Collection<Etudiant> etudiants = new Gson().fromJson(response, type);
                            etudiantList.clear();
                            etudiantList.addAll(etudiants);
                            filteredEtudiantList.addAll(etudiantList);
                            etudiantAdapter.notifyDataSetChanged();
                        } else {
                            Log.e(TAG, "Unexpected response format: " + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error fetching data: " + error.getMessage(), error);
            }
        });

        requestQueue.add(request);
    }

    private void filterList(String query) {
        filteredEtudiantList.clear();
        if (query.isEmpty()) {
            filteredEtudiantList.addAll(etudiantList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (Etudiant etudiant : etudiantList) {
                if (etudiant.getNom().toLowerCase().contains(lowerCaseQuery) ||
                        etudiant.getPrenom().toLowerCase().contains(lowerCaseQuery)) {
                    filteredEtudiantList.add(etudiant);
                }
            }
        }
        etudiantAdapter.notifyDataSetChanged();
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            Etudiant etudiantSupprime = filteredEtudiantList.get(position);

            new AlertDialog.Builder(ListActivity.this)
                    .setTitle("Confirmation de suppression")
                    .setMessage("Êtes-vous sûr de vouloir supprimer cet étudiant ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        supprimerEtudiant(etudiantSupprime);
                        etudiantList.remove(etudiantSupprime);
                        filteredEtudiantList.remove(position);
                        etudiantAdapter.notifyItemRemoved(position);
                    })
                    .setNegativeButton("Non", (dialog, which) -> {
                        etudiantAdapter.notifyItemChanged(position);
                    })
                    .show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.share) {
            String txt = "Check out my great Stars app!";
            String mimeType = "text/plain";
            ShareCompat.IntentBuilder
                    .from(this)
                    .setType(mimeType)
                    .setChooserTitle("Share this app via:")
                    .setText(txt)
                    .startChooser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void supprimerEtudiant(Etudiant etudiant) {
        StringRequest deleteRequest = new StringRequest(Request.Method.POST, "http://10.0.2.2/php_volley/ws/deleteEtudiant.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("DeleteEtudiant", "Response: " + response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                Toast.makeText(ListActivity.this, "Étudiant supprimé: " + etudiant.getNom(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ListActivity.this, "Erreur lors de la suppression: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ListActivity.this, "Erreur lors de la réponse du serveur", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("DeleteEtudiant", "Error: " + error.getMessage(), error);
                Toast.makeText(ListActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                return params;
            }
        };

        requestQueue.add(deleteRequest);
    }
    public void showEditDialog(Etudiant etudiant) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.etudiant_edit, null);
        dialogBuilder.setView(dialogView);

        TextView idView = dialogView.findViewById(R.id.idss);
        EditText nomEditText = dialogView.findViewById(R.id.editNom);
        EditText prenomEditText = dialogView.findViewById(R.id.editPrenom);
        EditText villeEditText = dialogView.findViewById(R.id.editVille);
        EditText sexeEditText = dialogView.findViewById(R.id.editSexe);
        ImageView imgView = dialogView.findViewById(R.id.img);

        // Set the current values
        idView.setText(String.valueOf(etudiant.getId()));
        nomEditText.setText(etudiant.getNom());
        prenomEditText.setText(etudiant.getPrenom());
        villeEditText.setText(etudiant.getVille());
        sexeEditText.setText(etudiant.getSexe());

        // Load the photo into the ImageView
        byte[] decodedString = Base64.decode(etudiant.getPhoto(), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        Glide.with(this)
                .load(bitmap)  // Assuming this returns the correct photo URL or Base64 string
                .placeholder(R.drawable.star)
                .error(R.drawable.error)
                .into(imgView);

        dialogBuilder.setTitle("Edit Student");
        dialogBuilder.setPositiveButton("Enregistrer", (dialog, which) -> {
            updateEtudiant(etudiant.getId(), nomEditText.getText().toString(),
                    prenomEditText.getText().toString(),
                    villeEditText.getText().toString(),
                    sexeEditText.getText().toString());
        });
        dialogBuilder.setNegativeButton("Annuler", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateEtudiant(int id, String nom, String prenom, String ville, String sexe) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://10.0.2.2/php_volley/ws/updateEtudiant.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        String message = jsonResponse.getString("message");

                        if (status.equals("success")) {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                            fetchEtudiants();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Error updating data: " + error.getMessage(), error);
                    Toast.makeText(this, "Error updating student. Please try again.", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(id));
                params.put("nom", nom);
                params.put("prenom", prenom);
                params.put("ville", ville);
                params.put("sexe", sexe);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }



}

