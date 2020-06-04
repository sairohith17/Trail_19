package gst.trainingcourse.instagramclone.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.FacebookSdk;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import gst.trainingcourse.instagramclone.R;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ShareBottomSheet extends BottomSheetDialogFragment {

    private ShareDialog mShareDialog;
    private Bitmap mBitmapShare;
    private Target mTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBitmapShare = bitmap;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    public static ShareBottomSheet newInstance(String url) {
        Bundle args = new Bundle();
        args.putString("urlPost", url);
        ShareBottomSheet fragment = new ShareBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        return inflater.inflate(R.layout.item_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView imgFacebook = view.findViewById(R.id.imgFaceBook);
        ImageView imgInstagram = view.findViewById(R.id.imgInstagram);
        ImageView imgMessenger = view.findViewById(R.id.imgMessenger);

        mShareDialog = new ShareDialog(this);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            Picasso.with(getContext()).load(bundle.getString("urlPost")).into(mTarget);
        }

        imgFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharePhoto sharePhoto = new SharePhoto.Builder()
                        .setBitmap(mBitmapShare)
                        .build();
                SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();
                mShareDialog.show(sharePhotoContent);
                checkBottomShow();
            }
        });

        imgInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                if (intent != null) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setPackage("com.instagram.android");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(mBitmapShare));
                    shareIntent.setType("image/*");

                    startActivity(shareIntent);
                    checkBottomShow();
                } else {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("market://details?id=" + "com.instagram.android"));
                    startActivity(intent);
                    checkBottomShow();
                }
            }
        });

        imgMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
                if (intent != null) {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.setPackage("com.facebook.orca");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(mBitmapShare));
                    shareIntent.setType("image/*");

                    startActivity(shareIntent);
                    checkBottomShow();
                }
            }
        });
    }

    private Uri getLocalBitmapUri(Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file =  new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image.png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
//            bmpUri = Uri.fromFile(file);
            bmpUri = FileProvider.getUriForFile(getContext(), getActivity().getPackageName()+".fileprovider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    private void checkBottomShow() {
        ShareBottomSheet shareBottomSheet = (ShareBottomSheet) getFragmentManager().findFragmentByTag("bottom");
        if (shareBottomSheet != null) {
            shareBottomSheet.dismiss();
        }
    }
}
