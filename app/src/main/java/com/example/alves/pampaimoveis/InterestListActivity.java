package com.example.alves.pampaimoveis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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


import java.util.List;

import static android.support.v4.app.NavUtils.navigateUpFromSameTask;

/**
 * An activity representing a list of Interests. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PropertyDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class InterestListActivity extends CommonActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());



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

        if (findViewById(R.id.property_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

       updateRecyclerView();
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

    private void updateRecyclerView(){
        // list of items
        View recyclerView = findViewById(R.id.property_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        TextView noneResult = (TextView) findViewById(R.id.textViewNoInterestResultFound);

        if(MainActivity.interestPropertyList.isEmpty()){
            noneResult.setVisibility(View.VISIBLE);
        }
        else{
            noneResult.setVisibility(View.GONE);
        }
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new InterestListActivity.SimpleItemRecyclerViewAdapter(MainActivity.interestPropertyList));
    }


    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<InterestListActivity.SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<Property> mValues;

        public SimpleItemRecyclerViewAdapter(List<Property> items) {
            mValues = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.property_list_content, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.city.setText(mValues.get(position).getCity());
            holder.neighborhood.setText(mValues.get(position).getNeighborhood());
            holder.street.setText(mValues.get(position).getStreet());
            holder.type.setText(mValues.get(position).getType());


            if(!MainActivity.interestPropertyList.isEmpty())
                if(MainActivity.interestPropertyList.contains(holder.mItem)){
                    holder.interestButton.setImageResource(R.mipmap.ic_star);
                }


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

            //TODO MAKE THIS ONCLICKLISTENER WORK WITHOUT GAMBIARRA
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(PropertyDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        PropertyDetailFragment fragment = new PropertyDetailFragment();

                        fragment.ARG_ITEM_ID = holder.mItem.getId(); //GAMB

                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.property_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, PropertyDetailActivity.class);
                        intent.putExtra(PropertyDetailFragment.ARG_ITEM_ID, holder.mItem.getId());

                        PropertyDetailFragment.ARG_ITEM_ID = holder.mItem.getId(); //GAMB
                        PropertyDetailActivity.isInterestList = true; //GAMB2

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView city;
            public final TextView neighborhood;
            public final TextView street;
            public final TextView type;
            public final ImageView photo;
            public final FloatingActionButton interestButton;
            public Property mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                photo = (ImageView) view.findViewById(R.id.houseImageView);
                city = (TextView) view.findViewById(R.id.cityTextView);
                neighborhood = (TextView) view.findViewById(R.id.neighborhoodTextView);
                street = (TextView) view.findViewById(R.id.streetTextView);
                type = (TextView) view.findViewById(R.id.typeTextView);
                interestButton = (FloatingActionButton) view.findViewById(R.id.favFloatingActionButton);

                interestButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (MainActivity.interestPropertyList.contains(mItem)) {
                            //remove announce from interest list
                            removeInterestConfirmation();
                        } else {
                            //adding announce to interest list
                            Interest interest = new Interest(MainActivity.firebaseUser.getUid(), mItem);
                            MainActivity.saveInterestToDataBase(interest);
                            interestButton.setImageResource(R.mipmap.ic_star);
                        }
                    }
                });
            }

            @Override
            public String toString() {
                return super.toString() + " '" + neighborhood.getText() + "'";
            }

            private void removeInterestConfirmation(){
                AlertDialog.Builder builder = new AlertDialog.Builder(InterestListActivity.this);
                builder.setTitle("Remover anúncio favorito");
                builder.setMessage("Você tem certeza que quer remover este anúncio da sua lista de favoritos?");

                // Set up the buttons
                builder.setPositiveButton("Tenho certeza", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Interest interest = new Interest(MainActivity.firebaseUser.getUid(), mItem);
                        MainActivity.removeInterest(interest);
                        interestButton.setImageResource(R.mipmap.ic_star_border);
                        updateRecyclerView();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }
}
