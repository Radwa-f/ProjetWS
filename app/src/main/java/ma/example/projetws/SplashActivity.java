package ma.example.projetws;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView logo = findViewById(R.id.logo);


        // Réduire le logo à 50% de sa taille initiale (échelle X et Y) en 3000 millisecondes
        logo.animate().scaleX(0.4f).scaleY(0.4f).setDuration(2000);

        // Rendre le logo complètement transparent (alpha 0) en 6000 millisecondes
        logo.animate().alpha(0f).setDuration(4000);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(4000);
                    Intent intent = new Intent(SplashActivity.this, AddEtudiant.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}