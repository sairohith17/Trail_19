package com.rohith.instaLovee.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.rohith.instaLovee.activity.PostActivity;
import com.rohith.instaLovee.R;
import com.rohith.instaLovee.fragments.BaseFragment;
import com.rohith.instaLovee.fragments.HomeFragment;
import com.rohith.instaLovee.fragments.NotificationFragment;
import com.rohith.instaLovee.fragments.PostDetailFragment;
import com.rohith.instaLovee.fragments.ProfileFragment;
import com.rohith.instaLovee.fragments.SearchFragment;
import com.rohith.instaLovee.control.FragmentUtils;
import com.rohith.instaLovee.control.StackListManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentInteractionCallback {

    public static final String TAB_HOME = "tab_home";
    public static final String TAB_SEARCH = "tab_search";
    public static final String TAB_NOTIFICATION = "tab_noti";
    public static final String TAB_PROFILE = "tab_profile";
    public static final String TAB_DETAIL = "tab_detail";

    private BottomNavigationView bottomNavigationView;
    private Map<String, Stack<String>> tagStacks;
    private String currentTab, current;
    private boolean check = true;
    private List<String> stackList;
    private List<String> menuStacks;
    private Fragment currentFragment;
    private Fragment homeFragment;
    private Fragment searchFragment;
    private Fragment notificationFragment;
    private Fragment profileFragment;

    public static final String ACTION = ".action";
    public static final String DATA_KEY_1 = ".data_key_1";
    public static final String DATA_KEY_2 = ".data_key_2";
    public static final String EXTRA_IS_ROOT_FRAGMENT = ".extra_is_root_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Enter keystore password:  01676445364
        //ga0RGNYHvNM5d0SLGQfpQWAPGJ8=

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);

        homeFragment = HomeFragment.newInstance(true);
        searchFragment = SearchFragment.newInstance(true);
        notificationFragment = NotificationFragment.newInstance(true);
        profileFragment = ProfileFragment.newInstance(true);

        tagStacks = new LinkedHashMap<>();
        tagStacks.put(TAB_HOME, new Stack<String>());
        tagStacks.put(TAB_SEARCH, new Stack<String>());
        tagStacks.put(TAB_NOTIFICATION, new Stack<String>());
        tagStacks.put(TAB_PROFILE, new Stack<String>());
        tagStacks.put(TAB_DETAIL, new Stack<String>());

        menuStacks = new ArrayList<>();
        menuStacks.add(TAB_HOME);

        stackList = new ArrayList<>();
        stackList.add(TAB_HOME);
        stackList.add(TAB_SEARCH);
        stackList.add(TAB_NOTIFICATION);
        stackList.add(TAB_PROFILE);
        stackList.add(TAB_DETAIL);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String publisher = bundle.getString("publisherid");
            current = bundle.getString("current");

            SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            Toast.makeText(getApplicationContext(), current, Toast.LENGTH_SHORT).show();

            if (current.equals(TAB_PROFILE)) {
                check = false;
                menuStacks = new ArrayList<>();
                menuStacks.add(current);
                bottomNavigationView.setSelectedItemId(R.id.navProfile);
            } else if (current.equals(TAB_SEARCH)) {
                check = false;
                menuStacks = new ArrayList<>();
                menuStacks.add(TAB_PROFILE);
                bottomNavigationView.setSelectedItemId(R.id.navProfile);
            } else if (current.equals(TAB_HOME)) {
                check = false;
                menuStacks = new ArrayList<>();
                menuStacks.add(TAB_PROFILE);
                bottomNavigationView.setSelectedItemId(R.id.navProfile);
            } else if (current.equals(TAB_NOTIFICATION)) {
                check = false;
                menuStacks = new ArrayList<>();
                menuStacks.add(TAB_PROFILE);
                bottomNavigationView.setSelectedItemId(R.id.navProfile);
            }
        } else {
            bottomNavigationView.setSelectedItemId(R.id.navHome);
        }
        bottomNavigationView.setOnNavigationItemReselectedListener(reselectedListener);
    }

    BottomNavigationView.OnNavigationItemReselectedListener reselectedListener = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navHome:
                    popStackExceptFirst();
                    break;
                case R.id.navSearch:
                    popStackExceptFirst();
                    break;
                case R.id.navFavorite:
                    popStackExceptFirst();
                    break;
                case R.id.navProfile:
                    popStackExceptFirst();
                    break;
            }
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navHome:
                    if (check) {
                        selectedTab(TAB_HOME, true);
                    } else {
                        selectedTab(TAB_PROFILE, false);
                        check = true;
                    }
                    return true;
                case R.id.navSearch:
                    if (check) {
                        selectedTab(TAB_SEARCH, true);
                    } else {
                        selectedTab(TAB_PROFILE, false);
                        check = true;
                    }
                    return true;
                case R.id.navAdd:
                    startActivity(new Intent(MainActivity.this, PostActivity.class));
                    return true;
                case R.id.navFavorite:
                    if (check) {
                        selectedTab(TAB_NOTIFICATION, true);
                    } else {
                        selectedTab(TAB_PROFILE, false);
                        check = true;
                    }
                    return true;
                case R.id.navProfile:
                    if (check) {
                        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                        editor.putString("profileid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        editor.apply();
                        selectedTab(TAB_PROFILE, true);
                    } else {
                        selectedTab(TAB_PROFILE, false);
                        check = true;
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onFragmentInteractionCallback(Bundle bundle) {
        String action = bundle.getString(ACTION);

        if (action != null) {
            switch (action) {
                case HomeFragment.ACTION_PROFILE:
                case NotificationFragment.ACTION_NOTI_PROFILE:
                case SearchFragment.ACTION_SREACH:
                case PostDetailFragment.ACTION_NEW_PROFILE:
                    showFragment(bundle, ProfileFragment.newInstance(false));
                    break;
                case NotificationFragment.ACTION_NOTI_POST_DETAIL:
                case ProfileFragment.ACTION_DETAIL_LIST:
                    showFragment(bundle, PostDetailFragment.newInstance(false));
                    break;
            }
        }
    }

    private void selectedTab(String tabId, boolean check) {
        currentTab = tabId;
        BaseFragment.setCurrentTab(currentTab);

        if (tagStacks.get(tabId).size() == 0) {
            switch (tabId) {
                case TAB_HOME:
                    FragmentUtils.addInitialTabFragment(getSupportFragmentManager(), tagStacks, TAB_HOME, homeFragment, R.id.fragmentContain, true);
                    resolveStackLists(tabId);
                    assignCurrentFragment(homeFragment);
                    break;
                case TAB_SEARCH:
                    FragmentUtils.addAdditionalTabFragment(getSupportFragmentManager(), tagStacks, TAB_SEARCH, searchFragment, currentFragment, R.id.fragmentContain, true);
                    resolveStackLists(tabId);
                    assignCurrentFragment(searchFragment);
                    break;
                case TAB_NOTIFICATION:
                    FragmentUtils.addAdditionalTabFragment(getSupportFragmentManager(), tagStacks, TAB_NOTIFICATION, notificationFragment, currentFragment, R.id.fragmentContain, true);
                    resolveStackLists(tabId);
                    assignCurrentFragment(notificationFragment);
                    break;
                case TAB_PROFILE:
                    if (check) {
                        FragmentUtils.addAdditionalTabFragment(getSupportFragmentManager(), tagStacks, TAB_PROFILE, profileFragment, currentFragment, R.id.fragmentContain, true);
                        resolveStackLists(tabId);
                        assignCurrentFragment(profileFragment);
                    } else {
                        FragmentUtils.addInitialTabFragment(getSupportFragmentManager(), tagStacks, TAB_PROFILE, profileFragment, R.id.fragmentContain, true);
                        resolveStackLists(tabId);
                        assignCurrentFragment(profileFragment);
                    }
                    break;
            }
        } else {
            Fragment targetFragment = getSupportFragmentManager().findFragmentByTag(tagStacks.get(tabId).lastElement());
            FragmentUtils.showHideTabFragment(getSupportFragmentManager(), targetFragment, currentFragment);
            resolveStackLists(tabId);
            assignCurrentFragment(targetFragment);
        }
    }

    private void popFragment() {
        String fragmentTag = tagStacks.get(currentTab).elementAt(tagStacks.get(currentTab).size() - 2);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);

        tagStacks.get(currentTab).pop();

        FragmentUtils.removeFragment(getSupportFragmentManager(), fragment, currentFragment);

        assignCurrentFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        int stackValue = 0;
        if (tagStacks.get(currentTab).size() == 1) {
            Stack<String> value = tagStacks.get(stackList.get(1));
            if (value.size() > 1) {
                stackValue = value.size();
                popAndNavigateToPreviousMenu();
            }
            if (stackValue <= 1) {
                if (menuStacks.size() > 1) {
                    navigateToPreviousMenu();
                } else {
                    finish();
                }
            }
        } else {
            popFragment();
        }
    }

    private void popAndNavigateToPreviousMenu() {
        String tempCurrent = stackList.get(0);
        currentTab = stackList.get(1);
        BaseFragment.setCurrentTab(currentTab);
        bottomNavigationView.setSelectedItemId(resolveTabPositions(currentTab));
        Fragment targetFragment = getSupportFragmentManager().findFragmentByTag(tagStacks.get(currentTab).lastElement());
        FragmentUtils.showHideTabFragment(getSupportFragmentManager(), targetFragment, currentFragment);
        assignCurrentFragment(targetFragment);
        StackListManager.updateStackToIndexFirst(stackList, tempCurrent);
        menuStacks.remove(0);
    }

    private void navigateToPreviousMenu() {
        menuStacks.remove(0);
        currentTab = menuStacks.get(0);
        Log.d("TAG", "navigateToPreviousMenu: " + currentTab);
        BaseFragment.setCurrentTab(currentTab);
        bottomNavigationView.setSelectedItemId(resolveTabPositions(currentTab));
        Fragment targetFragment = getSupportFragmentManager().findFragmentByTag(tagStacks.get(currentTab).lastElement());
        if (targetFragment != null && currentFragment != null)
            FragmentUtils.showHideTabFragment(getSupportFragmentManager(), targetFragment, currentFragment);
        assignCurrentFragment(targetFragment);
    }

    private void popStackExceptFirst() {
        if (tagStacks.get(currentTab).size() == 1) {
            return;
        }
        while (!tagStacks.get(currentTab).empty()
                && !getSupportFragmentManager().findFragmentByTag(tagStacks.get(currentTab).peek()).getArguments().getBoolean(EXTRA_IS_ROOT_FRAGMENT)) {
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(tagStacks.get(currentTab).peek()));
            tagStacks.get(currentTab).pop();
        }
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tagStacks.get(currentTab).elementAt(0));
        FragmentUtils.removeFragment(getSupportFragmentManager(), fragment, currentFragment);
        assignCurrentFragment(fragment);
    }

    private void showFragment(Bundle bundle, Fragment fragmentToAdd) {
        String tab = bundle.getString(DATA_KEY_1);
        boolean shouldAdd = bundle.getBoolean(DATA_KEY_2);
        FragmentUtils.addShowHideFragment(getSupportFragmentManager(), tagStacks, tab, fragmentToAdd, getCurrentFragmentFromShownStack(), R.id.fragmentContain, shouldAdd);
        assignCurrentFragment(fragmentToAdd);
    }

    private int resolveTabPositions(String currentTab) {
        int tabIndex = 0;
        switch (currentTab) {
            case TAB_HOME:
                tabIndex = R.id.navHome;
                break;
            case TAB_SEARCH:
                tabIndex = R.id.navSearch;
                break;
            case TAB_NOTIFICATION:
                tabIndex = R.id.navFavorite;
                break;
            case TAB_PROFILE:
                tabIndex = R.id.navProfile;
                break;
        }
        return tabIndex;
    }

    private void resolveStackLists(String tabId) {
        StackListManager.updateStackIndex(stackList, tabId);
        StackListManager.updateTabStackIndex(menuStacks, tabId);
    }

    private Fragment getCurrentFragmentFromShownStack() {
        return getSupportFragmentManager().findFragmentByTag(tagStacks.get(currentTab).elementAt(tagStacks.get(currentTab).size() - 1));
    }

    private void assignCurrentFragment(Fragment current) {
        currentFragment = current;
    }
}