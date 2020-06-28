package com.rohith.instaLovee.control;

import android.os.Bundle;

import java.util.Map;
import java.util.Stack;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.fragments.BaseFragment;

public class FragmentUtils {
    private static final String TAG_SEPARATOR = ":";
    private static final String ACTION = ".action";
    private static final String DATA_KEY_1 = ".data_key_1";
    private static final String DATA_KEY_2 = ".data_key_2";

    public static void addInitialTabFragment(FragmentManager fragmentManager,
                                             Map<String, Stack<String>> tagStacks,
                                             String tag,
                                             Fragment fragment,
                                             int layoutId,
                                             boolean shouldAddToStack) {
        fragmentManager
                .beginTransaction()
                .add(layoutId, fragment, fragment.getClass().getName() + TAG_SEPARATOR + fragment.hashCode())
                .commit();
        if (shouldAddToStack)
            tagStacks.get(tag).push(fragment.getClass().getName() + TAG_SEPARATOR + fragment.hashCode());
    }

    public static void addAdditionalTabFragment(FragmentManager fragmentManager,
                                                Map<String, Stack<String>> tagStacks,
                                                String tag,
                                                Fragment show,
                                                Fragment hide,
                                                int layoutId,
                                                boolean shouldAddToStack) {
        fragmentManager
                .beginTransaction()
                .add(layoutId, show, show.getClass().getName() + TAG_SEPARATOR + show.hashCode())
                .show(show)
                .hide(hide)
                .commit();
        if (shouldAddToStack)
            tagStacks.get(tag).push(show.getClass().getName() + TAG_SEPARATOR + show.hashCode());
    }

    public static void showHideTabFragment(FragmentManager fragmentManager,
                                           Fragment show,
                                           Fragment hide) {
        fragmentManager
                .beginTransaction()
                .hide(hide)
                .show(show)
                .commit();
    }

    public static void addShowHideFragment(FragmentManager fragmentManager,
                                           Map<String, Stack<String>> tagStacks,
                                           String tag,
                                           Fragment show,
                                           Fragment hide,
                                           int layoutId,
                                           boolean shouldAddToStack) {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
                .add(layoutId, show, show.getClass().getName() + TAG_SEPARATOR + show.hashCode())
                .show(show)
                .hide(hide)
                .commitAllowingStateLoss();
        if (shouldAddToStack)
            tagStacks.get(tag).push(show.getClass().getName() + TAG_SEPARATOR + show.hashCode());
    }

    public static void removeFragment(FragmentManager fragmentManager, Fragment show, Fragment remove) {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
                .remove(remove)
                .show(show)
                .commit();
    }

    public static void sendActionToActivity(String action, String tab, boolean shouldAdd, BaseFragment.FragmentInteractionCallback fragmentInteractionCallback) {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, action);
        bundle.putString(DATA_KEY_1, tab);
        bundle.putBoolean(DATA_KEY_2, shouldAdd);
        fragmentInteractionCallback.onFragmentInteractionCallback(bundle);
    }
}
