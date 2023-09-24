package bonfire.app.pos.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.List;

import bonfire.app.pos.adapter.TableAdapter;
import bonfire.app.pos.database.ServerManager;
import bonfire.app.pos.modules.DataModule;

public class TableActivity extends AppCompatActivity {
    List<DataModule> dataModules = new ArrayList<>();
    RecyclerView tableView;
    String table = "/GetTable";
    List<String> departList = new ArrayList<>();
    List<String> userList = new ArrayList<>();
    String username, password;
    ServerManager sm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_list);
        tableView = findViewById(R.id.tableView);
        tableView.hasFixedSize();
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            tableView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));

        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            tableView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 6));

        sm = new ServerManager(getApplicationContext());


        dataModules.clear();
        getTable();
        /*
        @SuppressLint("InflateParams") View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_login, null);
        final Spinner department = v.findViewById(R.id.spinDepart);
        final Spinner users = v.findViewById(R.id.spinUser);
        final EditText pasword = v.findViewById(R.id.userPass);
        final RadioGroup radioGroup = v.findViewById(R.id.radioGroup);
        final Button login = v.findViewById(R.id.login);

        {
            //for department spinner
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST,
                    new ServerManager(getApplicationContext()).getServer() + "/GetDepartment"
                    , new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject oj = new JSONObject(response);
                        JSONArray ja = oj.getJSONArray("DepartmentDetails");
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject a = ja.getJSONObject(i);
                            String de = a.getString("DepartmentName");
                            String id = a.getString("DepartmentID");
                            sm.setDeId(id);
                            departList.add(de);

                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                departList);
                        department.setAdapter(adapter);

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

        AlertDialog.Builder builder = new AlertDialog.Builder(TableActivity.this);
        builder.setView(v);
        final AlertDialog dialog = builder.create();

            dialog.show();
            dialog.setCancelable(false);


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
                                String username = oj.getString("username");
                                String id = oj.getString("Id");
                                String type = oj.getString("type");

                                String status = oj.getString("status");

                                if (status.matches("yes")) {
                                    sm.setUsername(username);
                                    sm.setType(type);
                                    sm.setUserId(id);
                                    dialog.dismiss();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("error", error.toString());
                            Toast.makeText(TableActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(TableActivity.this, "Please enter password", Toast.LENGTH_SHORT).show();

            }
        });

*/
    }

    public void getTable() {
        StringRequest sr = new StringRequest(Request.Method.POST,
                new ServerManager(getApplicationContext()).getServer() + table,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //    Log.e("table", response);
                        try {
                            JSONObject o = new JSONObject(response);
                            JSONArray a = o.getJSONArray("TableDetails");
                            for (int i = 0; i < a.length(); i++) {
                                JSONObject oo = a.getJSONObject(i);
                                DataModule m = new DataModule();
                                m.setTableid(oo.getString("table_id"));
                                m.setTablename(oo.getString("tablename"));
                                m.setIs_available(oo.getString("is_available"));
                                m.setTableno(oo.getString("tableno"));
                                m.setFirst_order(oo.getString("first_order_time"));
                                m.setLast_order(oo.getString("last_order_time"));
                                m.setTable_cost(oo.getString("table_cost"));
                                dataModules.add(m);
                            }
                            TableAdapter adapter = new TableAdapter(TableActivity.this, dataModules);
                            tableView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("tableError", error.toString());

            }
        }) {

        };
        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());
        rq.add(sr);
    }


}
