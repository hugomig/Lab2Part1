package e.g.hugom.lab2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText inptLogin;
    EditText inptPwd;

    Button btnAuth;

    TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inptLogin = findViewById(R.id.inptLogin);
        inptPwd = findViewById(R.id.inptPwd);

        btnAuth = findViewById(R.id.btnAuth);
        btnAuth.setOnClickListener(this);

        tvResult = findViewById(R.id.tvResult);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnAuth){
            //initialise la classe ThreadHttp avec le login et le mot d passe donne par l'utilisateur
            new ThreadHttp(inptLogin.getText().toString(),inptPwd.getText().toString()).start();
        }
    }

    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }

    public class ThreadHttp extends Thread{
        String login;
        String pwd;

        ThreadHttp(String login, String pwd){
            this.login=login;
            this.pwd=pwd;
        }

        @Override
        public void run(){
            URL url = null;
            try {
                url = new URL("https://httpbin.org/basic-auth/bob/sympa");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //Met le login et mot de passe dans la requete HTTP pour les passer au serveur
                String basicAuth = "Basic " + Base64.encodeToString((login+":"+pwd).getBytes(),
                        Base64.NO_WRAP);
                urlConnection.setRequestProperty ("Authorization", basicAuth);
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    String s = readStream(in);
                    Log.i("JFL", s);
                    try {
                        JSONObject jsonRes = new JSONObject(s);
                        boolean res = jsonRes.getBoolean("authenticated");
                        //Actualise le textView avec le true si la connexion est effectuee correctement
                        runOnUiThread(() -> tvResult.setText(""+res));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> tvResult.setText("false"));
            }
        }
    }
}