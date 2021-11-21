package ca.cmpt276.parentapp.UI.ConfigChild;

import static ca.cmpt276.parentapp.UI.ConfigChild.ConfigureChildActivity.saveKids;
import static ca.cmpt276.parentapp.UI.ConfigChild.ConfigureChildActivity.saveQueue;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import ca.cmpt276.parentapp.R;
import ca.cmpt276.parentapp.model.Child.ChildManager;
import ca.cmpt276.parentapp.model.Child.QueueManager;

public class EditChildActivity extends AppCompatActivity {
    private static final String INDEX_NAME = "ca.cmpt276.project.UI - index";
    private EditText name;
    private Bitmap childImage;
    private ChildManager childManager;
    private QueueManager queueManager;
    private int kidIndex;

    public static Intent makeIntent(Context context, int index) {
        Intent intent = new Intent(context, EditChildActivity.class);
        intent.putExtra(INDEX_NAME, index);
        return intent;
    }

    private void initItems() {
        name = findViewById(R.id.ChildNameEditText);
        childManager = ChildManager.getInstance();
        queueManager = QueueManager.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_child_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_and_edit_child_layout);


        initItems();
        getIndexFromIntent();
        fillInFields();
        saveEdited();
        addImgBtn();
    }

    private void addImgBtn() {
        ActivityResultLauncher<String> getContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        ImageView childImg = findViewById(R.id.ChildImageImageView);
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), result);
                            childImage = bitmap;
                            childImg.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        Button button = findViewById(R.id.addImgBtn);
        button.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(EditChildActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContent.launch("image/*");
            } else {
                requestStoragePermission();
            }
        });
    }

    // https://www.youtube.com/watch?v=SMrB97JuIoM&ab_channel=CodinginFlow
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed because of this and that")
                    .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(EditChildActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000))
                    .setNegativeButton("cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveEdited() {
        Button editChildBtn = findViewById(R.id.addChildToListBtn);
        editChildBtn.setOnClickListener(view -> {
            if (!name.getText().toString().equals("")) {
                childManager.getChildArrayList().get(kidIndex).setName(name.getText().toString());
                if (childImage != null) {
                    childManager.getChildArrayList().get(kidIndex).setImg(childImage);
                    queueManager.getQueueList().get(kidIndex).setImg(childImage);
                }
                saveQueue(EditChildActivity.this);
                saveKids(EditChildActivity.this);
                finish();
            } else {
                Toast.makeText(EditChildActivity.this, "Name Cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getIndexFromIntent() {
        Intent intent = getIntent();
        kidIndex = intent.getIntExtra(INDEX_NAME, 0);
    }

    private void fillInFields() {
        name.setText(childManager.getChildArrayList().get(kidIndex).getName());
        Button editChildBtn = findViewById(R.id.addChildToListBtn);
        editChildBtn.setText(R.string.edit_child_btn);

        if (childManager.getChildArrayList().get(kidIndex).getImg() != null) {
            ImageView childImg = findViewById(R.id.ChildImageImageView);
            Bitmap bitmap = childManager.getChildArrayList().get(kidIndex).getImg();

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 250, 250, true);

            childImg.setImageBitmap(resizedBitmap);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.helpBtn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditChildActivity.this);
            builder.setIcon(R.drawable.warning)
                    .setTitle("Closing Activity")
                    .setMessage("Are you sure you want to DELETE this kid?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        childManager.removeChild(kidIndex);
                        saveKids(EditChildActivity.this);
                        queueManager.removeChild(kidIndex);
                        saveQueue(EditChildActivity.this);
                        Toast.makeText(EditChildActivity.this, "KID DELETED", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.warning)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this setting without saving?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null)
                .show();
    }

}
