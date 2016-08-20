package vn.datsan.datsan.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vn.datsan.datsan.R;
import vn.datsan.datsan.models.Group;
import vn.datsan.datsan.models.UserRole;
import vn.datsan.datsan.serverdata.GroupManager;
import vn.datsan.datsan.ui.customwidgets.Alert.AlertInterface;
import vn.datsan.datsan.ui.customwidgets.Alert.SimpleAlert;

public class NewGroupActivity extends SimpleActivity {

    @BindView(R.id.group_name)
    EditText name;
    @BindView(R.id.phone)
    EditText phoneTv;
    @BindView(R.id.spinner_city)
    Spinner citySpinner;
    @BindView(R.id.take_photo)
    ImageButton takePhotoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        super.initToolBar();

        setTitle("Tạo đội bóng");
        ButterKnife.bind(this);
//        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(NewGroupActivity.this, "click", Toast.LENGTH_SHORT).show();
//                openImageIntent();
//            }
//        });
    }

    @OnClick(R.id.take_photo)
    public void onTakePhotoBtnClicked() {
        Toast.makeText(NewGroupActivity.this, "click", Toast.LENGTH_SHORT).show();
        openImageIntent();
    }
    @OnClick(R.id.register)
    public void onRegisterBtnClicked() {
        Group group = createGroup();
        if (group != null) {
            GroupManager.getInstance().addGroup(group, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        SimpleAlert.showAlert(NewGroupActivity.this, "Đăng ký thành công",
                                getString(R.string.close), null, new AlertInterface.OnTapListener() {
                                    @Override
                                    public void onTap(SimpleAlert alert, int buttonIndex) {
                                        finish();
                                    }
                                });
                    } else {
                        SimpleAlert.showAlert(NewGroupActivity.this, getString(R.string.error),
                                getString(R.string.failed_doagain),getString(R.string.close));
                    }
                }
            });
        }
    }

    private Uri outputFileUri;

    private Group createGroup() {
        String groupName = name.getText().toString();
        if (groupName.isEmpty()) {
            SimpleAlert.showAlert(NewGroupActivity.this, getString(R.string.error),
                    "Chưa nhập đủ thông tin !!!", getString(R.string.close));
            return null;
        }

        String location = citySpinner.getSelectedItem().toString();

        if (location.isEmpty()) {
            SimpleAlert.showAlert(NewGroupActivity.this, getString(R.string.error),
                    "Chưa nhập đủ thông tin !!!", getString(R.string.close));
            return null;
        }

        String phoneNumber = phoneTv.getText().toString();
        if (phoneNumber.isEmpty()) {
            SimpleAlert.showAlert(NewGroupActivity.this, getString(R.string.error),
                    "Chưa nhập đủ thông tin !!!", getString(R.string.close));
            return null;
        }

        List<String> phones = new ArrayList<>();
        phones.add(phoneNumber);

        Group group = new Group(groupName, 0, null);
        group.setCity(location);
        group.setPhones(phones);
        group.addMember(FirebaseAuth.getInstance().getCurrentUser().getUid(), UserRole.SUPER_ADMIN);
        return group;
    }

    private void openImageIntent() {

        // Determine Uri of camera image to save.
        final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
        root.mkdirs();
        final String fname = "xboyfname";//Utils.getUniqueImageFilename();
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_avatar));

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, 111);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 111) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
            }
        }
    }
}