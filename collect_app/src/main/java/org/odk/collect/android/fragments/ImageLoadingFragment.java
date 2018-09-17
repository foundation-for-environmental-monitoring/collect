package org.odk.collect.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.tasks.ImageLoadingTask;

public class ImageLoadingFragment extends Fragment {

    private ImageLoadingTask imageLoadingTask;
    private FormEntryActivity formEntryActivity;

    public void beginImageLoadingTask(Uri imageURi) {
        imageLoadingTask = new ImageLoadingTask(formEntryActivity);
        imageLoadingTask.execute(imageURi);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.formEntryActivity = (FormEntryActivity) activity;
        if (imageLoadingTask != null) {
            imageLoadingTask.onAttach(formEntryActivity);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (imageLoadingTask != null) {
            imageLoadingTask.onDetach();
        }
    }
}
