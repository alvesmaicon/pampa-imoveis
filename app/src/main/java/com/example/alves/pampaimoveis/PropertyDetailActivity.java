package com.example.alves.pampaimoveis;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * An activity representing a single Property detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link PropertyListActivity}.
 */
public class PropertyDetailActivity extends CommonActivity {

    public static FloatingActionButton fab;
    public static boolean isInterestList = false;
    public static ImageView imageViewAnnounceBanner;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fabFavProperty);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.interestPropertyList.contains(PropertyDetailFragment.mItem)) {
                    //remove announce from interest list
                    removeInterestConfirmation();
                } else {
                    //adding announce to interest list
                    Interest interest = new Interest(MainActivity.firebaseUser.getUid(), PropertyDetailFragment.mItem);
                    MainActivity.saveInterestToDataBase(interest);
                    fab.setImageResource(R.mipmap.ic_star_white);
                }
            }
        });

        imageViewAnnounceBanner = (ImageView) findViewById(R.id.image_announce_banner);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

        // setting the heigth of image at appbar equal to width of display
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        final int height = size.x;

        ViewGroup.LayoutParams params = appBarLayout.getLayoutParams();
        params.height = height;
        appBarLayout.setLayoutParams(params);

        appBarLayout.post(new Runnable() {
            @Override
            public void run() {
                // settin start height to half image of appbarLayout
                setAppBarOffset(height/2);
            }
        });





        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        PropertyDetailFragment fragment = null;
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(PropertyDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(PropertyDetailFragment.ARG_ITEM_ID));
            fragment = new PropertyDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.property_detail_container, fragment)
                    .commit();
        }



    }

    private void setAppBarOffset(int offsetPx){
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        behavior.onNestedPreScroll(coordinatorLayout, appBarLayout, null, 0, offsetPx, new int[]{0, 0});
    }

    private void removeInterestConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remover anúncio favorito");
        builder.setMessage("Você tem certeza que quer remover este anúncio da sua lista de favoritos?");

        // Set up the buttons
        builder.setPositiveButton("Tenho certeza", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Interest interest = new Interest(MainActivity.firebaseUser.getUid(), PropertyDetailFragment.mItem);
                MainActivity.removeInterest(interest);
                fab.setImageResource(R.mipmap.ic_star_white_border);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //

            if(isInterestList)
                navigateUpTo(new Intent(this, InterestListActivity.class));
            else
                navigateUpTo(new Intent(this, MainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
