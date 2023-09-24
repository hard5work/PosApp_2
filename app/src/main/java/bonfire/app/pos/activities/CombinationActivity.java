package bonfire.app.pos.activities;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pos.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import bonfire.app.pos.adapter.CartViewAdapter;
import bonfire.app.pos.adapter.MenuAdapter;
import bonfire.app.pos.database.SQLiteAdapter;
import bonfire.app.pos.database.ServerManager;
import bonfire.app.pos.modules.DataModule;

public class CombinationActivity extends AppCompatActivity {
    RecyclerView menuView;
    public static RecyclerView cartView;
    List<DataModule> dom = new ArrayList<>();
    List<DataModule> com = new ArrayList<>();

    String username, password;

    List<String> userList = new ArrayList<>();
    int orientation;
    public static  Button add, clear;
    ProgressDialog progress;
    RequestQueue rq;
    StringRequest sr;
    String itemID;
    Toolbar toolbar;
    ServerManager sm;
    SQLiteAdapter sa;
    CartViewAdapter adapter;
    String itemName;
    public static TextView totalAmt;
    public static int saSize;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.combine_list_cart_dish);
        menuView = findViewById(R.id.new_menu);
        cartView = findViewById(R.id.new_cart);
        add = findViewById(R.id.place_order);
        clear = findViewById(R.id.cancel_cart_list);
        totalAmt = findViewById(R.id.sumAmount);
        sm = new ServerManager(getApplicationContext());
        sa = new SQLiteAdapter(getApplicationContext());
        String mm = "Total : " + sa.getAmtT();
        totalAmt.setText(mm);
        itemID = getIntent().getStringExtra("itemID");
        progress = new ProgressDialog(CombinationActivity.this);
        progress.setMessage("Please Wait....");
        progress.setTitle("Loading..!!");
        progress.setCancelable(false);
        itemName= getIntent().getStringExtra("itemName");
        String menuName = " Catergory -" + itemName;
        setTitle(menuName);

        orientation = getResources().getConfiguration().orientation;

        menuView.hasFixedSize();
        cartView.hasFixedSize();
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            menuView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        } else
            menuView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));

        cartView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        dom.clear();
        foodList();
        com.clear();
        com = sa.getAllDatas();

        adapter = new CartViewAdapter(CombinationActivity.this, com);
        // adapter.notifyDataSetChanged();
        cartView.setAdapter(adapter);



       // Toast.makeText(this, "act " + saSize, Toast.LENGTH_SHORT).show();


            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saSize = new SQLiteAdapter(CombinationActivity.this).getAllDatas().size();
                    //  Toast.makeText(CombinationActivity.this, ">0", Toast.LENGTH_SHORT).show();
                    if (saSize > 0) {
                        onClicker();
                    }

                }
            });



        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteAdapter adapter1 = new SQLiteAdapter(getApplicationContext());
                adapter1.deleteDatabase();
                com = sa.getAllDatas();
                CartViewAdapter adapter = new CartViewAdapter(getApplicationContext(), com);
                adapter.notifyDataSetChanged();
                cartView.setAdapter(adapter);
                String mm = " Total :" + adapter1.getAmtT();
                totalAmt.setText(mm);
                //   ListDishesActivity.menuTextView.setText(String.valueOf(adapter1.getQntyT()));


            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView toolbarSearch = (SearchView) searchItem.getActionView();
        toolbarSearch.setQueryHint("Search.....");
        toolbarSearch.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert inputMethodManager != null;
                inputMethodManager.hideSoftInputFromWindow(toolbarSearch.getWindowToken(), 0);
                return true;
            }
        });

        toolbarSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()) {
                    dom.clear();
                    foodList();
                } else {
                    dom.clear();
                    foodList(s);
                }

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void foodList() {
        try {
            progress.show();
        }catch (Exception e){
            e.printStackTrace();
        }

        rq = Volley.newRequestQueue(Objects.requireNonNull(getApplicationContext()));
        sr = new StringRequest(Request.Method.POST, new ServerManager(getApplicationContext()).getServer() + "/GetProductCombo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                          //  Log.e("response", response);




                        try {
                            progress.dismiss();
                            JSONObject object = new JSONObject(response);

                            JSONArray jAry = object.getJSONArray("ProductDetails");

                            for (int i = 0; i < jAry.length(); i++) {
                                JSONObject jo = jAry.getJSONObject(i);

                                DataModule m = new DataModule();
                                List<DataModule> comboo = new ArrayList<>();
                                m.setItemID(jo.getInt("product_id"));
                                m.setItemImage(jo.getString("product_image"));
                                m.setItemName(jo.getString("productname"));
                                m.setItemPric(jo.getDouble("productprice"));
                                m.setProductDetail(jo.getString("productdetails"));
                                m.setIsComboItem(jo.getString("IsComboItem"));
                                String iscom = jo.getString("IsComboItem");
                                if (iscom.matches("True")) {


                                    JSONArray jam = jo.getJSONArray("ComboCat");
                                    for (int j = 0; j < jam.length(); j++) {
                                        JSONObject com = jam.getJSONObject(j);
                                        List<DataModule> ccombo = new ArrayList<>();
                                        DataModule n = new DataModule();
                                        n.setCCatId(com.getString("CCatId"));
                                        n.setCCategoryName(com.getString("CCategoryName"));
                                        JSONArray aj = com.getJSONArray("ComboCatItems");
                                        for (int k = 0; k < aj.length(); k++) {
                                            JSONObject oo = aj.getJSONObject(k);
                                            DataModule o = new DataModule();
                                            o.setCombo_product_id(oo.getString("Combo_product_id"));
                                            o.setCombo_product_name(oo.getString("Combo_productname"));
                                            ccombo.add(o);
                                        }
                                        n.setCombo(ccombo);

                                        comboo.add(n);
                                    }
                                    m.setCombos(comboo);
                                }

                                dom.add(m);


                            }

                            MenuAdapter adapter = new MenuAdapter(CombinationActivity.this, dom);
                            menuView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Log.e("error", error.toString());
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
//

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("CategoryID", itemID);
                return map;
            }
        };
        rq.add(sr);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void foodList(final String s) {
        rq = Volley.newRequestQueue(Objects.requireNonNull(getApplicationContext()));
        sr = new StringRequest(Request.Method.POST, new ServerManager(getApplicationContext()).getServer() + "/GetProductCombo",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //    Log.e("response", response);

                        try {
                            JSONObject object = new JSONObject(response);

                            JSONArray jAry = object.getJSONArray("ProductDetails");

                            for (int i = 0; i < jAry.length(); i++) {
                                JSONObject jo = jAry.getJSONObject(i);

                                DataModule m = new DataModule();
                                List<DataModule> comboo = new ArrayList<>();
                                List<DataModule> ccombo = new ArrayList<>();
                                m.setItemID(jo.getInt("product_id"));
                                m.setItemImage(jo.getString("product_image"));
                                m.setItemName(jo.getString("productname"));
                                String s1 = jo.getString("productname");
                                m.setItemPric(jo.getDouble("productprice"));
                                m.setProductDetail(jo.getString("productdetails"));
                                m.setIsComboItem(jo.getString("IsComboItem"));
                                String iscom = jo.getString("IsComboItem");
                                if (iscom.matches("True")) {


                                    JSONArray jam = jo.getJSONArray("ComboCat");
                                    for (int j = 0; j < jam.length(); j++) {
                                        JSONObject com = jam.getJSONObject(j);
                                        DataModule n = new DataModule();
                                        n.setCCatId(com.getString("CCatId"));
                                        n.setCCategoryName(com.getString("CCategoryName"));
                                        JSONArray aj = com.getJSONArray("ComboCatItems");
                                        for (int k = 0; k < aj.length(); k++) {
                                            JSONObject oo = aj.getJSONObject(k);
                                            DataModule o = new DataModule();
                                            o.setCombo_product_id(oo.getString("Combo_product_id"));
                                            o.setCombo_product_name(oo.getString("Combo_productname"));
                                            ccombo.add(o);
                                        }
                                        n.setCombo(ccombo);

                                        comboo.add(n);
                                    }
                                    m.setCombos(comboo);
                                }

                                if (s1.toUpperCase().contains(s.toUpperCase()))
                                    dom.add(m);


                            }

                            MenuAdapter adapter = new MenuAdapter(CombinationActivity.this, dom);
                            menuView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
//

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("CategoryID", itemID);
                return map;
            }
        };
        rq.add(sr);
    }

    public void onClicker() {
        @SuppressLint("InflateParams") View v =
                LayoutInflater.from(CombinationActivity.this)
                        .inflate(R.layout.new_login_layout, null);
        final EditText department = v.findViewById(R.id.spinDepart);
        final Spinner users = v.findViewById(R.id.spinUser);
        final EditText pasword = v.findViewById(R.id.userPass);
        final Button login = v.findViewById(R.id.login);
        AlertDialog.Builder builder =
                new AlertDialog.Builder(CombinationActivity.this);
        builder.setView(v);
        final AlertDialog dialog = builder.create();

        if (!sm.isLoggedIn()) {
            dialog.show();
            dialog.setCancelable(false);
        }
        else
            {

            Intent intent = new Intent(CombinationActivity.this, TableActivity.class);
           // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
           // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
         //   CombinationActivity.this.finish();

        }






        {
            //for userspinner
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST,
                    new ServerManager(getApplicationContext()).getServer() + "/GetUserList"
                    , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject oj = new JSONObject(response);
                        JSONArray ja = oj.getJSONArray("UserDetails");
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject a = ja.getJSONObject(i);
                            String de = a.getString("UserName");
                            String id = a.getString("UserID");
                            userList.add(de);

                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_dropdown_item_1line, userList);
                        users.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(request);
        }

        users.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                username = adapterView.getItemAtPosition(i).toString();
                //    Toast.makeText(TableActivity.this, "username " + username, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                password = pasword.getText().toString().trim();

                if (password.length() > 0) {

                    //     Toast.makeText(TableActivity.this, Build.DEVICE, Toast.LENGTH_SHORT).show();
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    StringRequest request = new StringRequest(Request.Method.POST,
                            new ServerManager(getApplicationContext()).getServer() + "/Login"
                            , new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //   Toast.makeText(TableActivity.this, response, Toast.LENGTH_SHORT).show();
                            try {

                                JSONObject oj = new JSONObject(response);


                                String status = oj.getString("status");

                                if (status.matches("yes")) {
                                    String username = oj.getString("username");
                                    String id = oj.getString("Id");
                                    String type = oj.getString("type");
                                    dialog.dismiss();
                                    sm.setUsername(username);
                                    String com = department.getText().toString();
                                    sm.setRemarks(com);
                                    sm.setType(type);
                                    sm.setUserId(id);
                                    Intent intent = new Intent(CombinationActivity.this, TableActivity.class);
                                 //   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                  //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                  //  CombinationActivity.this.finish();
                                }
                                if (status.matches("no")){
                                    Toast.makeText(CombinationActivity.this, "Enter Valid Password", Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                dialog.dismiss();
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error", error.toString());
                            dialog.dismiss();
                            Toast.makeText(CombinationActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded; charset=UTF-8";
                        }


                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> map = new HashMap<>();
                            map.put("UserName", username);
                            map.put("Password", password);
                            map.put("device_id", "1");
                            map.put("device_type", "123");
                            return map;
                        }
                    };
                    requestQueue.add(request);
                } else
                    Toast.makeText(CombinationActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();

            }
        });


/*
                    View commenter = LayoutInflater.from(getApplicationContext()).inflate(R.layout.new_login_layout, null);
                    final EditText inst = commenter.findViewById(R.id.addcomment);
                    final Button submit = commenter.findViewById(R.id.submitComment);

                    AlertDialog.Builder builder = new AlertDialog.Builder(CombinationActivity.this);
                    builder.setView(commenter);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String com = inst.getText().toString();
                            sm.setRemarks(com);




                        }
                    });
*/


    }

}
