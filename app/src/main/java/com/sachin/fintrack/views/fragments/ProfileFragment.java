package com.sachin.fintrack.views.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sachin.fintrack.AdmobAds.Admob;
import com.sachin.fintrack.R;
import com.sachin.fintrack.databinding.FragmentProfileBinding;
import com.sachin.fintrack.models.UserModel;
import com.sachin.fintrack.views.activites.LoginActivity;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;
    ProgressDialog progressDialog;
    private boolean isCloudinaryInitialized = false;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Cloudinary Setup
        initCloudinary();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Profile Update");
        progressDialog.setMessage("Uploading to Cloudinary...");
        progressDialog.setCancelable(false);

        Admob.loadBannerAd(binding.bannerAd, getActivity());

        String webUrl = "https://moccasin-leandra-70.tiiny.site/";
        binding.privacyPolicy.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))));
        binding.contact.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))));

        binding.fetchImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 2);
        });

        binding.logout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            getActivity().finish();
        });

        loadUserData();

        return binding.getRoot();
    }

    private void initCloudinary() {
        try {
            Map config = new HashMap();
            config.put("cloud_name", "dpwh7vma3");
            config.put("api_key", "521133164344568");
            config.put("api_secret", "adoX4hQ45X_SWa5C7XrBMvFXA9M");
            MediaManager.init(getContext(), config);
        } catch (Exception e) {
            // Agar pehle se init hai toh error ignore karein
        }
    }

    private void loadUserData() {
        if (auth.getUid() == null) return;

        firestore.collection("users").document(auth.getUid())
                .addSnapshotListener((value, error) -> {
                    if (value != null && value.exists()) {
                        UserModel model = value.toObject(UserModel.class);
                        if (model != null) {
                            binding.usersName.setText(model.getName());
                            binding.usersEmail.setText(model.getEmail());

                            if (model.getProfile() != null && !model.getProfile().isEmpty()) {
                                Picasso.get()
                                        .load(model.getProfile())
                                        .networkPolicy(NetworkPolicy.NO_CACHE)
                                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                                        .placeholder(R.drawable.friend_2)
                                        .into(binding.profileImage);
                            }
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && data != null && data.getData() != null) {
            uploadToCloudinary(data.getData());
        }
    }

    private void uploadToCloudinary(Uri uri) {
        progressDialog.show();

        MediaManager.get().upload(uri).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {}

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {}

            @Override
            public void onSuccess(String requestId, Map resultData) {
                String finalUrl = (String) resultData.get("secure_url");

                // Firestore update (Ab ye automatic hoga)
                firestore.collection("users").document(auth.getUid())
                        .update("profile", finalUrl)
                        .addOnSuccessListener(unused -> {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Profile Updated via Cloudinary!", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Cloudinary Error: " + error.getDescription(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {}
        }).dispatch();
    }
}