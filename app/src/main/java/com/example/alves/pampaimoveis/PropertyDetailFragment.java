package com.example.alves.pampaimoveis;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * A fragment representing a single Property detail screen.
 * This fragment is either contained in a {@link PropertyListActivity}
 * in two-pane mode (on tablets) or a {@link PropertyDetailActivity}
 * on handsets.
 */
public class PropertyDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    public static Property mItem;
    private User userOwner;



    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PropertyDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            for(Property property : MainActivity.propertyList){
                if (property.getId().equals(ARG_ITEM_ID)){
                    mItem = property;
                    break;
                }
            }

            //TODO showing current userOwner to owner of a property. Change this to userOwner owner of property.
            for(User user: MainActivity.userOwnerList){
                if(user.getId().equals(mItem.getUserId())){
                    userOwner = user;
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
        View rootView = inflater.inflate(R.layout.property_detail, container, false);
        //TODO aqui é preciso atualizar a DetailActivity com o conteúdo do mItem (Property)
        if (mItem != null) {

            /*TODO Exibir detalhes do imóvel de acordo com o tipo
            aluguel, venda, ou república
                se é aluguel exibir informação disponível para alugar ou não
                se é república exibir informação de quantas vagas estão disponíveis
                    tambem é possível que exista preços diferentes para vagas diferentes em uma mesma república (Será adicionado depois)
            Exibir gridlayout de fotos, e exibir a foto ampliada em um frame sobreposto
            Exibir informações do usuário
                Nome, telefone e email e foto


             */

            ((TextView) rootView.findViewById(R.id.textViewTypeDetail)).setText(mItem.getType());
            ((TextView) rootView.findViewById(R.id.textViewAreaDetail)).setText(mItem.getArea() + "m²");
            ((TextView) rootView.findViewById(R.id.textViewCityDetail)).setText(mItem.getCity());
            ((TextView) rootView.findViewById(R.id.textViewStreetDetail)).setText(mItem.getStreet());
            ((TextView) rootView.findViewById(R.id.textViewNeighborhoodDetail)).setText(mItem.getNeighborhood());
            ((TextView) rootView.findViewById(R.id.textViewNumberDetail)).setText(mItem.getNumber());
            ((TextView) rootView.findViewById(R.id.textViewCEPDetail)).setText(mItem.getCep());
            ((TextView) rootView.findViewById(R.id.textViewComplementDetail)).setText(mItem.getComplement());
            ((TextView) rootView.findViewById(R.id.textViewPriceDetail)).setText("R$ " + mItem.getPrice());
            ((TextView) rootView.findViewById(R.id.textViewRoomsDetail)).setText(mItem.getRooms());
            ((TextView) rootView.findViewById(R.id.textViewBathroomsDetail)).setText(mItem.getBathrooms());


            if(mItem.getPhotoList().size() > 0) {
                try {
                    new DownloadImageTask(PropertyDetailActivity.imageViewAnnounceBanner)
                            .execute(mItem.getPhotoList().get(0));
                } catch (Exception e) {
                }
            }

            try {
                new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView0))
                        .execute(mItem.getPhotoList().get(0));
            } catch (Exception e) {
            }

            try {
                new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView1))
                        .execute(mItem.getPhotoList().get(1));
            } catch (Exception e) {
            }

            try {
                new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView2))
                        .execute(mItem.getPhotoList().get(2));
            } catch (Exception e) {
            }

            try {
                new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView3))
                        .execute(mItem.getPhotoList().get(3));
            } catch (Exception e) {
            }

            try {
                new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView4))
                        .execute(mItem.getPhotoList().get(4));
            } catch (Exception e) {
            }

            try {
                new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageView5))
                        .execute(mItem.getPhotoList().get(5));
            } catch (Exception e) {
            }




            // Dificuldade em buscar este userOwner.
            if(userOwner != null) {
                if (userOwner.getName() != null) {
                    ((TextView) rootView.findViewById(R.id.textViewAnnounceUser)).setText(userOwner.getName());
                    ((TextView) rootView.findViewById(R.id.textViewAnnounceUserEmail)).setText(userOwner.getEmail());
                    ((TextView) rootView.findViewById(R.id.textViewAnnounceUserPhone)).setText(userOwner.getCel());
                }


                if (userOwner.getPhotourl() != null)
                    try {
                        new DownloadImageTask((ImageView) rootView.findViewById(R.id.imageViewAnnounceUser))
                                .execute(userOwner.getPhotourl());
                    } catch (Exception e) {
                    }


            }
            // Setting fab icon in property detail activity
            if (!MainActivity.interestPropertyList.isEmpty())
                if (MainActivity.interestPropertyList.contains(mItem)) {
                    PropertyDetailActivity.fab.setImageResource(R.mipmap.ic_star_white);
                }
        }

        return rootView;
    }
}
