package ma.example.projetws;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ma.example.projetws.beans.Etudiant;

public class AddEtudiant extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AddEtudiant";
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText nom;
    private EditText prenom;
    private Spinner ville;
    private RadioButton m;
    private RadioButton f;
    private Button add;
    private Button list;
    private Button selectImage;
    private ImageView imageView;

    private RequestQueue requestQueue;
    private String insertUrl = "http://10.0.2.2/php_volley/ws/createEtudiant.php";
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);
        Toolbar toolbar = findViewById(R.id.toolbar0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Accueil");
        TextView toolbarTitle = (TextView) toolbar.getChildAt(0);
        toolbarTitle.setTypeface(toolbarTitle.getTypeface(), Typeface.BOLD);
        Drawable overflowIcon = toolbar.getOverflowIcon();
        if (overflowIcon != null) {
            overflowIcon.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
        }

        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        ville = findViewById(R.id.ville);
        add = findViewById(R.id.add);
        list = findViewById(R.id.list);
        selectImage = findViewById(R.id.selectImage);
        imageView = findViewById(R.id.imageView);
        m = findViewById(R.id.m);
        f = findViewById(R.id.f);

        add.setOnClickListener(this);
        list.setOnClickListener(this);
        selectImage.setOnClickListener(this);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onClick(View v) {
        if (v == list) {
            Intent intent = new Intent(AddEtudiant.this, ListActivity.class);
            startActivity(intent);
        }
        if (v == selectImage) {
            openGallery();
        }
        if (v == add) {
            if (validateInputs()) {
                uploadImage();
            }
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri);
        }
    }

    private void uploadImage() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String imageString = Base64.encodeToString(byteArray, Base64.DEFAULT); // Use NO_WRAP to avoid new lines
            sendDataToServer(imageString);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendDataToServer(final String imageString) {
        StringRequest request = new StringRequest(Request.Method.POST,
                insertUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response: " + response);
                if (response.startsWith("[")) {
                    Type type = new TypeToken<Collection<Etudiant>>() {}.getType();
                    Collection<Etudiant> etudiants = new Gson().fromJson(response, type);
                    for (Etudiant e : etudiants) {
                        Log.d(TAG, e.toString());
                    }
                    Toast.makeText(AddEtudiant.this, "Ajout d'etudiant", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Unexpected response format: " + response);
                    Toast.makeText(AddEtudiant.this, "Ajout d'etudiant", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", "Error: " + error.getMessage(), error);
                Log.e(TAG, "Error response code: " + error.networkResponse.statusCode);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String sexe = m.isChecked() ? "homme" : "femme";
                HashMap<String, String> params = new HashMap<>();
                params.put("nom", nom.getText().toString());
                params.put("prenom", prenom.getText().toString());
                params.put("ville", ville.getSelectedItem().toString());
                params.put("sexe", sexe);
                params.put("photo", imageString);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private boolean validateInputs() {
        if (nom.getText().toString().isEmpty() || prenom.getText().toString().isEmpty() ||
                ville.getSelectedItem() == null || (!m.isChecked() && !f.isChecked()) || imageUri == null) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return false;
        }
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
}
