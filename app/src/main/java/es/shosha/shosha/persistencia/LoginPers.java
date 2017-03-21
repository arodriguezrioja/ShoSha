package es.shosha.shosha.persistencia;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import es.shosha.shosha.dominio.Lista;
import es.shosha.shosha.dominio.Usuario;
import es.shosha.shosha.persistencia.sqlite.AdaptadorBD;

public class LoginPers extends AsyncTask<String, Void, Boolean> {
    private final static String URL_GET = "http://shosha.jiraizoz.es/loginUsuario.php?";
    private final static String ATRIBUTO_EMAIL = "email=";
    private final static String ATRIBUTO_PASS = "pass=";
    private List<Lista> lListas = null;

    private Context contexto;
    private final CountDownLatch count;

    public LoginPers(Context c, CountDownLatch cdl) {
        this.contexto = c;
        count = cdl;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        Usuario usuario = null;
        Boolean iniciadoSesion = false;

        if (params.length == 2) {
            try {
                String email = URLEncoder.encode(params[0], "UTF-8");
                String pass = URLEncoder.encode(params[1], "UTF-8");
                java.net.URL urlObj = new URL(URL_GET + ATRIBUTO_EMAIL + email + "&" + ATRIBUTO_PASS + pass);

                HttpURLConnection lu = (HttpURLConnection) urlObj.openConnection();

                BufferedReader rd = new BufferedReader(new InputStreamReader(lu.getInputStream()));
                String line = "", res = "";
                while ((line = rd.readLine()) != null) {
                    res += line;
                }
                rd.close();

                usuario = jsonParser(res);
                if(usuario == null){
                    iniciadoSesion = false;
                } else {

                    iniciadoSesion = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                lanzadorExcepcion();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return iniciadoSesion;
    }

    protected void onPostExecute(List<Lista> listas) {
        this.lListas = listas;
    }

    private Usuario jsonParser(String data) {

        Usuario u = null;
        try {
            JSONObject jso = new JSONObject(data);
            Integer success = jso.getInt("success");
            System.out.println("LOGIN============ " + success);
            JSONArray usuarios = jso.getJSONArray("usuario");
            if (success == 1) {
                JSONObject usuario = usuarios.getJSONObject(0);
                u = new Usuario(usuario.getString("id"), usuario.getString("nombre"), usuario.getString("email"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return u;
    }

    private void lanzadorExcepcion() throws Exception {
        throw new Exception("En ListaPers, error en los parámetros de ejecución.");
    }

    private void insertarBD(Lista l) {
        AdaptadorBD adap = new AdaptadorBD(this.contexto);
        adap.open();
        try {
            adap.insertarLista(l.getId(), l.getNombre(), l.getPropietario(), l.isEstado() ? "1" : "0");
        } finally {
            adap.close();
        }
    }
}
