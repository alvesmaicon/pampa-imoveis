package com.example.alves.pampaimoveis;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends CommonActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static List<Property> interestPropertyList = new ArrayList<>();
    public static List<Interest> interestList = new ArrayList<>();
    public static List<Property> propertyList = new ArrayList<>();
    public static List<User> userOwnerList = new ArrayList<>();
    public static List<Property> oldPropertyList = new ArrayList<>();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static FirebaseUser firebaseUser;
    private DatabaseReference userReference, propertyListReference, userOwnerReference;
    private Query interestListReference;
    private ValueEventListener userValueEventListener, propertyListValueEventListener,
            interestListValueEventListener, userOwnerListValueEventListener;
    public static User user;
    private FloatingActionButton fab;


    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Firebase is going to save data on app cache


        initUser();

        mAuth = FirebaseAuth.getInstance();

        firebaseUser = mAuth.getCurrentUser();

        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    callLoginActivity();
                }
            }
        };

        // Query list of interest with firebaseUser.UID
        // Two listeners
        //TODO MAKE THIS SELECT BETTER

        /* Retrieving data from interest child in database*/
        interestListReference = Utils.getDatabase().getReference()
                .child("interest")
                .orderByChild("userId")
                .equalTo(firebaseUser.getUid());

        interestListValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                interestList.clear();
                for(DataSnapshot interestSnapshot : dataSnapshot.getChildren()){
                    Interest interest = interestSnapshot.getValue(Interest.class);
                        interestList.add(interest);
                }
                updateInterestPropertyList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        /*Retrieving data from uers child*/

        userOwnerReference = Utils.getDatabase().getReference().child("users");

        userOwnerListValueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userOwnerList.clear();

                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    User user = userSnapshot.getValue(User.class);
                    userOwnerList.add(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        /* Retrieving data from property child*/
        propertyListReference = Utils.getDatabase().getReference().child("property");

        propertyListValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                propertyList.clear();

                for(DataSnapshot propertySnapshot : dataSnapshot.getChildren()){
                    Property property = propertySnapshot.getValue(Property.class);
                    propertyList.add(property);
                }

                updateInterestPropertyList();
                updateRecyclerView();

                oldPropertyList.clear();
                oldPropertyList = new ArrayList<>(propertyList);
                //Collections.copy(oldPropertyList, propertyList);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        userReference = Utils.getDatabase().getReference()
        .child("users").child(firebaseUser.getUid());

        userValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user != null)
                    updateView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Database error
            }
        };




        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callFilterDialog();
            }

        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        if (findViewById(R.id.property_detail_container) != null) {
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
        mAuth.addAuthStateListener(mAuthListener);
        userReference.addValueEventListener(userValueEventListener);
        propertyListReference.addValueEventListener(propertyListValueEventListener);
        userOwnerReference.addValueEventListener(userOwnerListValueEventListener);

        //INTEREST LIST LISTENER WILL REMAIN OPEN
        interestListReference.addValueEventListener(interestListValueEventListener);
    }


    @Override
    public void onResume(){
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
        userReference.addValueEventListener(userValueEventListener);
        propertyListReference.addValueEventListener(propertyListValueEventListener);
        interestListReference.addValueEventListener(interestListValueEventListener);
        userOwnerReference.addValueEventListener(userOwnerListValueEventListener);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);

        if(userValueEventListener != null)
            userReference.removeEventListener(userValueEventListener);

        if(propertyListValueEventListener != null)
            propertyListReference.removeEventListener(propertyListValueEventListener);

        if(userOwnerListValueEventListener != null)
            userOwnerReference.removeEventListener(userOwnerListValueEventListener);
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mAuthListener != null)
            mAuth.removeAuthStateListener(mAuthListener);

        if(userValueEventListener != null)
            userReference.removeEventListener(userValueEventListener);

        if(propertyListValueEventListener != null)
            propertyListReference.removeEventListener(propertyListValueEventListener);

        if(userOwnerListValueEventListener != null)
            userOwnerReference.removeEventListener(userOwnerListValueEventListener);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_announce) {
            callPropertyAnnounce();
        } else if (id == R.id.nav_favorite) {
            callInterests();
        } else if (id == R.id.nav_profile) {
            callMyProfile();
        } else if (id == R.id.nav_my_properties) {
            callMyProperties();
        } else if (id == R.id.nav_manage) {
            callSettings();
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new MainActivity.SimpleItemRecyclerViewAdapter(propertyList));
    }

    private void updateInterestPropertyList(){
        interestPropertyList.clear();
        for(Interest interest : interestList){
            for(Property property : propertyList){
                if(property.getId().equals(interest.getPropertyid())){
                    interestPropertyList.add(property);
                }
            }
        }
    }

    private void updateRecyclerView(){
        View recyclerView = findViewById(R.id.property_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        TextView noneResult = (TextView) findViewById(R.id.textViewNoResultFound);

        if(propertyList.isEmpty()){
            noneResult.setVisibility(View.VISIBLE);
        }
        else{
            noneResult.setVisibility(View.GONE);
        }
    }


    private void signOut(){
        mAuth.signOut();
    }

    private void callFilterDialog(){
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.search_dialog);

        // gamb for custom dialog color items
        dialog.setTitle(Html.fromHtml("<font color='#767176'> Filtrar busca</font>"));

        try {
            Resources resources = dialog.getContext().getResources();

            int titleDividerId = resources.getIdentifier("titleDivider", "id", "android");
            View titleDivider = dialog.getWindow().getDecorView().findViewById(titleDividerId);
            titleDivider.setBackgroundColor(Color.argb(0, 0, 0, 0)); // change divider color to transparent
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // set the custom dialog components - text, image and button

        final TextView CityFilter = (TextView) dialog.findViewById(R.id.editTextFilterCity);
        final TextView NeighborhoodFilter = (TextView) dialog.findViewById(R.id.editTextFilterNeighborhood);
        final Spinner TypeFilter = (Spinner) dialog.findViewById(R.id.spinnerFilterType);
        final TextView PriceFilter = (TextView) dialog.findViewById(R.id.editTextFilterPrice);


        Button dialogButtonOk = (Button) dialog.findViewById(R.id.dialogButtonOK);
        // if button is clicked, the list content is filtered
        dialogButtonOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String City = CityFilter.getText().toString().trim();
                String Neighborhood = NeighborhoodFilter.getText().toString().trim();
                String Price = PriceFilter.getText().toString().trim();
                String Type = TypeFilter.getSelectedItem().toString();

                propertyList.clear();
                propertyList = new ArrayList<>(oldPropertyList);
                //Collections.copy(propertyList, oldPropertyList);


                if(!Type.equals("Selecione o tipo do anúncio")){
                    for(Iterator<Property> iterator = propertyList.iterator(); iterator.hasNext(); ){
                        if(!iterator.next().getType().equals(Type)){
                            iterator.remove();
                        }
                    }
                }

                if(!City.isEmpty()){
                    for(Iterator<Property> iterator = propertyList.iterator(); iterator.hasNext(); ){
                        if(!iterator.next().getCity().toLowerCase().matches("(?i).*" + City.toLowerCase() + ".*")){
                            iterator.remove();
                        }
                    }
                }

                if(!Neighborhood.isEmpty()){
                    for(Iterator<Property> iterator = propertyList.iterator(); iterator.hasNext(); ){
                        if(!iterator.next().getNeighborhood().toLowerCase().matches("(?i).*" + Neighborhood.toLowerCase() + ".*")){
                            iterator.remove();
                        }
                    }
                }

                if(!Price.isEmpty()){
                    for(Iterator<Property> iterator = propertyList.iterator(); iterator.hasNext(); ){
                        if(Float.parseFloat(iterator.next().getPrice()) > Float.parseFloat(Price)){
                            iterator.remove();
                        }
                    }
                }

                dialog.dismiss();
                updateRecyclerView();

            }
        });

        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.dialogButtonCancel);
        // if button is clicked, close the custom dialog
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void callLoginActivity(){
        Intent intent = new Intent( this, LoginActivity.class );
        startActivity(intent);
        finish();
    }


    private void callInterests(){
        Intent intent = new Intent( this, InterestListActivity.class );
        startActivity(intent);
    }


    private void callPropertyAnnounce(){
        Intent intent = new Intent( this, PropertyAnnounceActivity.class );
        startActivity(intent);
    }


    private void callSettings(){
        Intent intent = new Intent( this, SettingsActivity.class );
        startActivity(intent);
    }


    private void callMyProfile(){
        Intent intent = new Intent( this, ProfileActivity.class );
        startActivity(intent);
    }


    private void callMyProperties(){
        Intent intent = new Intent( this, PropertyListActivity.class );
        startActivity(intent);
    }


    private void initUser(){
        user = new User();
    }


    private void updateView(){
        TextView userNameHeader = (TextView) findViewById(R.id.textViewUserNameHeader);
        TextView userEmailHeader = (TextView) findViewById(R.id.textViewUserEmailHeader);

        if(user.getName() != null && user.getEmail() != null) {
            userNameHeader.setText(user.getName());
            userEmailHeader.setText(user.getEmail());
            // setting profile pic

            //TODO: TESTAR SE A IMAGEM É NULL ANTES DE POR NA VIEW
            // se o url é invalido dá problema
            if(user.getPhotourl()!=null)
            try{
                new DownloadImageTask((ImageView) findViewById(R.id.imageUserHeader))
                        .execute(user.getPhotourl());
            } catch(Exception e) {
                Snackbar.make(fab, "Não foi possível exibir a foto do usuário", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }




    // Adapter
    //TODO: enhancement this method
    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

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

            if(!interestPropertyList.isEmpty())
                if(interestPropertyList.contains(holder.mItem)){
                    holder.interestButton.setImageResource(R.mipmap.ic_star);
                }

            if(mValues.get(position).getPhotoList().size() != 0)
                if(!mValues.get(position).getPhotoList().get(0).isEmpty())
                try{
                    new DownloadImageTask(holder.photo)
                            .execute(mValues.get(position).getPhotoList().get(0));
                } catch(Exception e) {
                    showSnackbar("Não foi possível exibir a foto");
                }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(PropertyDetailFragment.ARG_ITEM_ID, holder.mItem.getId());
                        PropertyDetailFragment fragment = new PropertyDetailFragment();

                        //TODO more gamb
                        fragment.ARG_ITEM_ID = holder.mItem.getId();

                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.property_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, PropertyDetailActivity.class);
                        intent.putExtra(PropertyDetailFragment.ARG_ITEM_ID, holder.mItem.getId());

                        //TODO this is a gamb
                        PropertyDetailFragment.ARG_ITEM_ID = holder.mItem.getId();
                        PropertyDetailActivity.isInterestList = false;

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
                        if (interestPropertyList.contains(mItem)) {
                            //remove announce from interest list
                            removeInterestConfirmation();
                        } else {
                            //adding announce to interest list
                            Interest interest = new Interest(firebaseUser.getUid(), mItem);
                            saveInterestToDataBase(interest);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Remover anúncio favorito");
                builder.setMessage("Você tem certeza que quer remover este anúncio da sua lista de favoritos?");

                // Set up the buttons
                builder.setPositiveButton("Tenho certeza", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Interest interest = new Interest(firebaseUser.getUid(), mItem);
                        removeInterest(interest);
                        interestButton.setImageResource(R.mipmap.ic_star_border);
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

    protected static void saveInterestToDataBase(Interest interest){
        Utils.getDatabase().getReference().child("interest").child(interest.getId()).setValue(interest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("Success", "Interest saved to database.");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                FirebaseCrash.report( e );
            }
        });
    }

    protected static void removeInterest(Interest interest){
        Utils.getDatabase().getReference().child("interest").child(interest.getId()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Success", "Interest removed.");
                        }
                    }
                });
    }
}
