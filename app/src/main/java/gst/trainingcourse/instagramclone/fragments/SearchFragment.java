package gst.trainingcourse.instagramclone.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gst.trainingcourse.instagramclone.R;
import gst.trainingcourse.instagramclone.adapters.UsersAdapter;
import gst.trainingcourse.instagramclone.models.Users;

public class SearchFragment extends BaseFragment {

    private RecyclerView mRecyclerViewUsers;
    private UsersAdapter mUsersAdapter;
    private List<Users> mListUsers = new ArrayList<>();
    private EditText mEdtSearch;

    private static final String SEARCH_FRAGMENT = "SearchFragment";
    public static final String ACTION_SREACH = SEARCH_FRAGMENT + "action.search";
    private static final String EXTRA_IS_ROOT_FRAGMENT = ".extra_is_root_fragment";

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(boolean isRoot) {

        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_ROOT_FRAGMENT, isRoot);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        setAdapter();
        readUsers();
        initAction();
    }

    private void initView(View view) {
        mRecyclerViewUsers = view.findViewById(R.id.recyclerviewUsers);
        mEdtSearch = view.findViewById(R.id.edtSearch);
    }

    private void initAction() {
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setAdapter() {
        mUsersAdapter = new UsersAdapter(getContext(), mListUsers, true, ACTION_SREACH, currentTab, fragmentInteractionCallback);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerViewUsers.setLayoutManager(linearLayoutManager);
        mRecyclerViewUsers.setAdapter(mUsersAdapter);
    }

    private void searchUsers(String s) {
        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("username")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mListUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Users users = snapshot.getValue(Users.class);
                    mListUsers.add(users);
                }
                mUsersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mEdtSearch.getText().toString().equals("")) {
                    mListUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Users users = snapshot.getValue(Users.class);
                        mListUsers.add(users);
                    }
                    mUsersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
