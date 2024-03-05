package com.example.alves.pampaimoveis;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * An activity representing a list of Properties. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link InterestDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class PropertyListActivity extends CommonActivity {

    public static final List<Property> propertyList = new ArrayList<>();

    private Query propertyListReference;
    private ValueEventListener propertyListValueEventListener;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        /* Retrieving data from property child*/
        assert firebaseUser != null;
        propertyListReference = Utils.getDatabase().getReference()
                .child("property")
                .orderByChild("userId")
                .equalTo(firebaseUser.getUid());

        propertyListValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();

                for (DataSnapshot propertySnapshot : dataSnapshot.getChildren()) {
                    Property property = propertySnapshot.getValue(Property.class);


                        propertyList.add(property);

                }

                //list items
                View recyclerView = findViewById(R.id.interest_list);
                assert recyclerView != null;
                setupRecyclerView((RecyclerView) recyclerView);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        if (findViewById(R.id.interest_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        propertyListReference.addValueEventListener(propertyListValueEventListener);
    }


    @Override
    public void onResume(){
        super.onResume();
        propertyListReference.addValueEventListener(propertyListValueEventListener);
    }


    @Override
    public void onStop() {
        super.onStop();

        if(propertyListValueEventListener != null)
            propertyListReference.removeEventListener(propertyListValueEventListener);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        if(propertyListValueEventListener != null)
            propertyListReference.removeEventListener(propertyListValueEventListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new PropertyListActivity.SimpleItemRecyclerViewAdapter(propertyList));
    }

    class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<PropertyListActivity.SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Property> mValues;

        SimpleItemRecyclerViewAdapter(List<Property> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.interest_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.city.setText(mValues.get(position).getCity());
            holder.neighborhood.setText(mValues.get(position).getNeighborhood());
            holder.date.setText(mValues.get(position).getStartdate());
            holder.type.setText(mValues.get(position).getType());

            if(mValues.get(position).getPhotoList().size() != 0) {
                if (!mValues.get(position).getPhotoList().get(0).isEmpty()) {
                    try {
                        new DownloadImageTask(holder.photo)
                                .execute(mValues.get(position).getPhotoList().get(0));
                    } catch (Exception e) {
                        showSnackbar("Não foi possível exibir a foto");
                    }
                }
            }

            holder.mView.setOnLongClickListener(new View.OnLongClickListener(){

                @Override
                public boolean onLongClick(View v) {
                    Snackbar.make(v, "Menu de contexto ainda não implementado", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    return true;
                }
            });

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        InterestDetailFragment fragment = new InterestDetailFragment();
                        Bundle arguments = new Bundle();
                        arguments.putString(InterestDetailFragment.ARG_ITEM_ID, holder.mItem.getId());


                        //TODO mais gamb
                        fragment.ARG_ITEM_ID = holder.mItem.getId();


                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.interest_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, InterestDetailActivity.class);
                        intent.putExtra(InterestDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        //TODO tentando fazer funcionar com gamb  pois não estava passando por argumentos VER ISTO COM MAIS CALMA
                        InterestDetailFragment.ARG_ITEM_ID = holder.mItem.getId();

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView city;
            final TextView neighborhood;
            final TextView date;
            final TextView type;
            final ImageView photo;
            Property mItem;

            ViewHolder(View view) {
                super(view);
                mView = view;
                photo = (ImageView) view.findViewById(R.id.houseImageView);
                city = (TextView) view.findViewById(R.id.cityTextView);
                neighborhood = (TextView) view.findViewById(R.id.neighborhoodTextView);
                date = (TextView) view.findViewById(R.id.dateTextView);
                type = (TextView) view.findViewById(R.id.typeTextView);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + neighborhood.getText() + "'";
            }
        }
    }
}
