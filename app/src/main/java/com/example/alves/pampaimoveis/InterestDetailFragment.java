package com.example.alves.pampaimoveis;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A fragment representing a single Interest detail screen.
 * This fragment is either contained in a {@link InterestListActivity}
 * in two-pane mode (on tablets) or a {@link InterestDetailActivity}
 * on handsets.
 */
public class InterestDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private Property mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public InterestDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            for(Property property : PropertyListActivity.propertyList){
                if(property.getId().equals(ARG_ITEM_ID)){
                    mItem = property;
                    break;
                }
            }

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getType() + " em " + mItem.getNeighborhood());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.interest_detail, container, false);

        // Show the content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.interest_detail)).setText("Id: " + mItem.getId() + ".\nBairro: " + mItem.getNeighborhood() + ".\nRua: " + mItem.getStreet() + ".\nTipo: " + mItem.getType() + ".");
        }

        return rootView;
    }
}
