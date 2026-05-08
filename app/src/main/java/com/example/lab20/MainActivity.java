package com.example.lab20;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etKeyword;
    private ContactAdapter adapter;
    private final List<Contact> contactList = new ArrayList<>();
    private ContactApi contactApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLoadContacts = findViewById(R.id.btnLoadContacts);
        Button btnSyncContacts = findViewById(R.id.btnSyncContacts);
        Button btnSearch = findViewById(R.id.btnSearch);
        etKeyword = findViewById(R.id.etKeyword);
        RecyclerView recyclerViewContacts = findViewById(R.id.recyclerViewContacts);

        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(contactList);
        recyclerViewContacts.setAdapter(adapter);

        contactApi = RetrofitClient.getClient().create(ContactApi.class);

        btnLoadContacts.setOnClickListener(v -> checkPermissionAndLoadContacts());
        btnSyncContacts.setOnClickListener(v -> syncContactsToServer());
        btnSearch.setOnClickListener(v -> searchContacts());
    }

    private void checkPermissionAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    loadContacts();
                } else {
                    Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
                }
            });

    private void loadContacts() {
        contactList.clear();

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        )
                );

                String phone = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                        )
                );

                contactList.add(new Contact(name, phone));
            }
            cursor.close();
        }

        adapter.updateData(contactList);
        Toast.makeText(this, "Contacts chargés : " + contactList.size(), Toast.LENGTH_SHORT).show();
    }

    private void syncContactsToServer() {
        if (contactList.isEmpty()) {
            checkPermissionAndLoadContacts();
            if (contactList.isEmpty()) {
                Toast.makeText(this, "Aucun contact à synchroniser", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        contactApi.insertMultipleContacts(contactList).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur réseau : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(this, "Synchronisation lancée pour " + contactList.size() + " contacts", Toast.LENGTH_SHORT).show();
    }

    private void searchContacts() {
        String keyword = etKeyword.getText().toString().trim();

        if (keyword.isEmpty()) {
            Toast.makeText(this, "Saisir un nom ou un numéro", Toast.LENGTH_SHORT).show();
            return;
        }

        contactApi.searchContacts(keyword).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call, @NonNull Response<List<Contact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur lors de la recherche : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
