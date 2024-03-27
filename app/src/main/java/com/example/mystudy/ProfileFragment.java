package com.example.mystudy;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView userEmailTextView;
    private TextView userInfoTextView;
    private TextView userGroupTextView;
    private FirebaseUser currentUser;
    private FirebaseAuth auth;
    private Map<String, Integer> surnameToValueMap;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private EditText userSurnameEditText;
    private Button applyGroupButton;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        profileImageView = view.findViewById(R.id.profile_picture);
        userEmailTextView = view.findViewById(R.id.user_email);
        userInfoTextView = view.findViewById(R.id.user_info);
        userGroupTextView = view.findViewById(R.id.user_group);
        auth = FirebaseAuth.getInstance();
        initSurnameToValueMap();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            userEmailTextView.setText(userEmail);
            userSurnameEditText = view.findViewById(R.id.user_surname);
            String userSurname = userSurnameEditText.getText().toString().trim();
            if (!userSurname.isEmpty() && surnameToValueMap.containsKey(userSurname)) {
                int userValue = surnameToValueMap.get(userSurname);
                String userInfo = "User Information: Sample Info";
                String userGroup = "User Group: " + userValue;
                userInfoTextView.setText(userInfo);
                userGroupTextView.setText(userGroup);
            } else {
                String userInfo = "User Information: Sample Info";
                String userGroup = "User Group: Невизначено";
                userInfoTextView.setText(userInfo);
                userGroupTextView.setText(userGroup);
            }
            profileImageView.setImageResource(R.drawable.baseline_person_24);
            profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFileChooser();
                }
            });
        } else {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        }

        applyGroupButton = view.findViewById(R.id.apply_group_btn);
        applyGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userSurname = userSurnameEditText.getText().toString().trim();
                if (!userSurname.isEmpty() && surnameToValueMap.containsKey(userSurname)) {
                    int userValue = surnameToValueMap.get(userSurname);
                    String userGroup = "User Group: " + userValue;
                    userGroupTextView.setText(userGroup);
                } else {
                    String userGroup = "User Group: Невизначено";
                    userGroupTextView.setText(userGroup);
                }
            }
        });

        Button logoutButton = view.findViewById(R.id.logout_btn);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                profileImageView.setImageURI(imageUri);
                uploadPicture();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadPicture() {
        if (imageUri != null) {
            final ProgressDialog pd = new ProgressDialog(requireContext());
            pd.setTitle("Uploading Image...");
            pd.setCancelable(false);
            pd.show();

            final String randomKey = UUID.randomUUID().toString();
            StorageReference riversRef = storageRef.child(randomKey);

            UploadTask uploadTask = riversRef.putFile(imageUri);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pd.dismiss();
                    Snackbar.make(requireView(), "Image Uploaded.", Snackbar.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(requireContext(), "Failed to Upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    pd.setMessage("Percentage: " + (int) progressPercent + "%");
                }
            });
        } else {
            Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void initSurnameToValueMap
    () {
        surnameToValueMap = new HashMap<>();
        surnameToValueMap.put("Babii", 1);
        surnameToValueMap.put("Bandurov", 1);
        surnameToValueMap.put("Baran", 1);
        surnameToValueMap.put("Basarab", 1);
        surnameToValueMap.put("Bentsa", 1);
        surnameToValueMap.put("Berdar", 1);
        surnameToValueMap.put("Blaha", 1);
        surnameToValueMap.put("Bohdan", 1);
        surnameToValueMap.put("Bombushkar", 1);
        surnameToValueMap.put("Buncha", 1);
        surnameToValueMap.put("Vdoviuk", 1);
        surnameToValueMap.put("Havryltso", 1);
        surnameToValueMap.put("Haisyn", 1);
        surnameToValueMap.put("Hanulych", 1);
        surnameToValueMap.put("Hehedosh", 1);
        surnameToValueMap.put("Hleba", 1);
        surnameToValueMap.put("Hretchyn", 1);
        surnameToValueMap.put("Hutii", 1);
        surnameToValueMap.put("Dadus", 1);
        surnameToValueMap.put("Danatsko", 1);
        surnameToValueMap.put("Dancha", 1);
        surnameToValueMap.put("Deriabina", 1);
        surnameToValueMap.put("Dolynko", 2);
        surnameToValueMap.put("Yevsieiev", 2);
        surnameToValueMap.put("Kampov", 2);
        surnameToValueMap.put("Kanchii", 2);
        surnameToValueMap.put("Kaniuk", 2);
        surnameToValueMap.put("Karpikov", 2);
        surnameToValueMap.put("Karpushyn", 2);
        surnameToValueMap.put("Kachmar", 2);
        surnameToValueMap.put("Keretsman", 2);
        surnameToValueMap.put("Kovalenko", 2);
        surnameToValueMap.put("Koval", 2);
        surnameToValueMap.put("Kozak", 2);
        surnameToValueMap.put("Korol", 2);
        surnameToValueMap.put("Korolchuk", 2);
        surnameToValueMap.put("Kosovilka", 2);
        surnameToValueMap.put("Kryvliak", 2);
        surnameToValueMap.put("Kuznetsov", 2);
        surnameToValueMap.put("Kuzma", 2);
        surnameToValueMap.put("Kuleshov", 2);
        surnameToValueMap.put("Ledney", 2);
        surnameToValueMap.put("Lys", 2);
        surnameToValueMap.put("Lohoida", 3);
        surnameToValueMap.put("Liakh", 3);
        surnameToValueMap.put("Markus", 3);
        surnameToValueMap.put("Miko", 3);
        surnameToValueMap.put("Minhazov", 3);
        surnameToValueMap.put("Nyshchyi", 3);
        surnameToValueMap.put("Polyak", 3);
        surnameToValueMap.put("Ponepoliak", 3);
        surnameToValueMap.put("Pop", 3);
        surnameToValueMap.put("Popenko", 3);
        surnameToValueMap.put("Popovych", 3);
        surnameToValueMap.put("Rusyn", 3);
        surnameToValueMap.put("Riabinchak", 4);
        surnameToValueMap.put("Savko", 4);
        surnameToValueMap.put("Soldatenko", 4);
        surnameToValueMap.put("Starov", 4);
        surnameToValueMap.put("Stryzhak", 4);
        surnameToValueMap.put("Tereshchenko", 4);
        surnameToValueMap.put("Tokar", 4);
        surnameToValueMap.put("Trompak", 4);
        surnameToValueMap.put("Trofymchuk", 4);
        surnameToValueMap.put("Tyasko", 4);
        surnameToValueMap.put("Feketa", 4);
        surnameToValueMap.put("Fuchak", 4);
        surnameToValueMap.put("Tsibelenko", 4);
        surnameToValueMap.put("Tsoninets", 4);
        surnameToValueMap.put("Cheketa", 4);
        surnameToValueMap.put("Chepurnyi", 4);
        surnameToValueMap.put("Chizhmar", 4);
        surnameToValueMap.put("Chonka", 4);
        surnameToValueMap.put("Chup", 4);
        surnameToValueMap.put("Shein", 4);
        surnameToValueMap.put("Sherehii", 4);
        surnameToValueMap.put("Shyshmarov", 4);
        surnameToValueMap.put("Yanchov", 4);
        surnameToValueMap.put("Yashchuk", 4);
    }
}
