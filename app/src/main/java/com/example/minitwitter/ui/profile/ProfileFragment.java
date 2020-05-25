package com.example.minitwitter.ui.profile;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.minitwitter.common.Constantes;
import com.example.minitwitter.common.MyApp;
import com.example.minitwitter.data.ProfileViewModel;
import com.example.minitwitter.R;
import com.example.minitwitter.retrofit.request.RequestUserProfile;
import com.example.minitwitter.retrofit.response.ResponseUserProfile;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;

    ImageView ivAvatar;
    EditText etUsername, etEmail, etPassword, etWebSite, etDescripcion;
    Button btnSave, btnChangePassword;
    boolean loginData = true;
    PermissionListener allPermissionListener;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.profile_fragment, container, false);

        ivAvatar = v.findViewById(R.id.imageViewAvatar);
        etUsername = v.findViewById(R.id.editTextUserName);
        etEmail = v.findViewById(R.id.editTextEmail);
        etWebSite = v.findViewById(R.id.editTextWebSite);
        etDescripcion = v.findViewById(R.id.editTextDescription);
        etPassword = v.findViewById(R.id.editTextCurrentPassword);
        btnSave = v.findViewById(R.id.buttonSave);
        btnChangePassword = v.findViewById(R.id.buttonChangePassword);

        btnSave.setOnClickListener(new View.OnClickListener() {

            String username = etUsername.getText().toString();
            String email = etEmail.getText().toString();
            String descripcion = etDescripcion.getText().toString();
            String website = etWebSite.getText().toString();
            String password = etPassword.getText().toString();

            @Override
            public void onClick(View v) {

                if (username.isEmpty()) {
                    etUsername.setError("El nombre de usuario es requerido");
                } else if (email.isEmpty()) {
                    etEmail.setError("El email es requerido");
                } else if (password.isEmpty()) {
                    etPassword.setError("La contraseña es requerida");
                } else {
                    RequestUserProfile requestUserProfile = new RequestUserProfile(username, email, descripcion, website, password);
                    profileViewModel.updateProfile(requestUserProfile);
                    Toast.makeText(MyApp.getContext(), "Enviando informacion al servidor", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(false);
                }
            }
        }) ;

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Click on save", Toast.LENGTH_SHORT).show();

            }
        }) ;

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Invocar a la seleccion de la fotografia
                checkPermissions();
            }
        });

        //ViewModel
        profileViewModel.userProfile.observe(getActivity(), new Observer<ResponseUserProfile>() {
            @Override
            public void onChanged(ResponseUserProfile responseUserProfile) {
                loginData = false;
                etUsername.setText(responseUserProfile.getUsername());
                etEmail.setText(responseUserProfile.getEmail());
                etWebSite.setText(responseUserProfile.getWebsite());
                etDescripcion.setText(responseUserProfile.getDescripcion());

                if (!responseUserProfile.getPhotoUrl().isEmpty()){
                    Glide.with(getActivity())
                            .load(Constantes.API_MINITWITTER_FILES_URL + responseUserProfile.getPhotoUrl())
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop()
                            .skipMemoryCache(true)
                            .into(ivAvatar);
                }
                if (!loginData) {
                    btnSave.setEnabled(true);
                    Toast.makeText(MyApp.getContext(),"Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return v;
    }

    private void checkPermissions() {
        PermissionListener dialogOnDeniedPermissionListener =
                DialogOnDeniedPermissionListener.Builder.withContext(getActivity())
                .withTitle("Permisos")
                .withMessage("Los permisos solicitados son necesarios para poder seleccionar una foto de perfil")
                .withButtonText("Aceptar")
                .withIcon(R.mipmap.ic_launcher)
                .build();

        allPermissionListener = new CompositePermissionListener(
                (PermissionListener) getActivity(), dialogOnDeniedPermissionListener
        );

        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(allPermissionListener)
                .check(); 
    }
}
