package es.shosha.shosha;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import es.shosha.shosha.Adaptadores.AdapterLista;
import es.shosha.shosha.dominio.Lista;
import es.shosha.shosha.persistencia.ListaFB;
import es.shosha.shosha.persistencia.sqlite.AdaptadorBD;

public class ListasActivas extends AppCompatActivity {

    private ListView list;
    private Lista listaClicada;
    List<Lista> listas = new ArrayList<>();
    AdapterLista adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // obtener listas del usuario
        AdaptadorBD abd = new AdaptadorBD(getBaseContext());
        abd.open();

        listas = abd.obtenerListas(MyApplication.getUser().getId());
        Log.d("lis", listas.toString());

        setContentView(R.layout.activity_listas_activas);
        list = (ListView) findViewById(R.id.listasActivas);
        setListas(listas);

        abd.close();

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        AdaptadorBD abd = new AdaptadorBD(getBaseContext());
        abd.open();
        listas.removeAll(listas);
        listas.addAll(abd.obtenerListas(MyApplication.getUser().getId()));
        abd.close();
        adaptador.notifyDataSetChanged();

    }

    public void setListas(List<Lista> listas2) {
        adaptador = new AdapterLista(this, listas2);
        list.setAdapter(adaptador);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(ListasActivas.this, ListaProductos.class);
                Bundle bundle = new Bundle();
                bundle.putInt("idLista", adaptador.getItem(position).getId());
                i.putExtras(bundle);
                startActivity(i);
            }
        });
        // Registramos el menu contextual
        registerForContextMenu(list);
        //Aparece el botón de atrás
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Mostrar menú para las listas activas
        getMenuInflater().inflate(R.menu.menu_listas_activas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.anadir_lista:
                Intent i = new Intent(this, ListaManual.class);
                startActivity(i);
                return true;
            case R.id.escanear_QR:
                Intent i1 = new Intent(this, LectorQR.class);
                startActivity(i1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Se crea el Context Menu, añadiendo las opciones deseadas
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        ListView lv = (ListView) v;
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle("Opciones");
        menu.add(0, v.getId(), 0, "Eliminar");
        menu.add(0, v.getId(), 0, "Editar");
        listaClicada = (Lista) lv.getItemAtPosition(acmi.position);
    }

    /**
     * Se definen las funciones que se llamarán al pulsar las
     * opciones del Context Menu
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String opcion = item.getTitle().toString();
        int id = listaClicada.getId();
        String idu = String.valueOf(MyApplication.getUser().getId());
        if (opcion.equals("Eliminar")) {
            //Se elimina la lista seleccionada de las listas del usuario
            Toast.makeText(this, "Eliminando lista " + id, Toast.LENGTH_SHORT).show();
            ListaFB.borrarListaFB(id, listaClicada.getPropietario().getId() == MyApplication.getUser().getId());
            //Registra la eliminación en la abse de datos local, para recogerla correctamente al recargar la actividad
            AdaptadorBD abd=new AdaptadorBD(getBaseContext());
            abd.open();
            abd.eliminarLista(id,MyApplication.getUser().getId());
            abd.close();

            listas.remove(listaClicada);
            adaptador.notifyDataSetChanged();

        } else if (opcion.equals("Editar")) {
            Intent i = new Intent(ListasActivas.this, EditarLista.class);
            Bundle bundle = new Bundle();
            bundle.putInt("idLista", id);
            i.putExtras(bundle);
            startActivity(i);
        } else {
            return false;
        }
        return true;
    }

    public void mostrarAnadirLista(View view) {
        //Ejecuta la actividad de listas activas
        Intent i = new Intent(this, AnadirLista.class);
        startActivity(i);
    }
}
