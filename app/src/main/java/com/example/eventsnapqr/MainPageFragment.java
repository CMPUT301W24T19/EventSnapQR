package com.example.eventsnapqr;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * the main page of EventSnapQR. Allows the user to go to scanQR to check-in, go to organize
 * new event to setup an event, go to browse events. in the top right is a link to the users
 * profile, and if the user has admin privileges there is an admin button in the top left to
 * enter admin mode
 */
public class MainPageFragment extends Fragment {
    private Button buttonOrganizeEvent;
    private Button buttonAdminMainPage;
    private Button buttonBrowseEvent;
    private ExtendedFloatingActionButton buttonScanQR;
    private ImageView buttonViewProfile;
    private String androidId;
    private ProgressBar progressBar, carouselProgressBar;
    private MaterialCardView carouselCardView;
    private CardView viewUserCard;
    private TextView upComingEvent;
    private List<View> views;
    private boolean isSnapHelperAttached = false;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * authenticate the current user to verify if they have admin privileges. this method
     * determines if the admin button is visible or not
     */
    private void authenticateUser(){
        FirebaseController.Authenticator listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (!exists) {
                    User user = new User(androidId, androidId, null, null, null);
                    FirebaseController.getInstance().addUser(user, new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                            for (View view: views) {
                                view.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                    for (View view: views) {
                        view.setVisibility(View.VISIBLE);
                    }
                }
            }
            @Override
            public void onAdminExistenceChecked(boolean exists) {
                if(exists && !buttonAdminMainPage.isShown()){
                    buttonAdminMainPage.setVisibility(View.VISIBLE);
                }else{
                    buttonAdminMainPage.setVisibility(View.INVISIBLE);
                }
            }
        };
        FirebaseController.checkUserExists(androidId, listener);

    }
    public interface ImageUriCallback {
        void onImageUrisLoaded(List<String> imageUris, List<String> eventNames);
    }

    public void getImageUris(ImageUriCallback callback) {
        FirebaseFirestore.getInstance().collection("events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("TAG", "Listen failed.", error);
                    return;
                }

                List<String> imageUris = new ArrayList<>();
                List<String> eventNames = new ArrayList<>();
                int count = 0;
                for (QueryDocumentSnapshot doc : value) {
                    if (count >= 6) break;
                    String posterUri = (String) doc.getData().get("posterURI");
                    String eventName = (String) doc.getString("eventName");
                    if (posterUri != null) {
                        imageUris.add(posterUri);
                        eventNames.add(eventName);
                        count++;
                    }
                }

                callback.onImageUrisLoaded(imageUris, eventNames);
            }
        });
    }

    public List<String> eventImages;
    /**
     * handles button presses throughout the fragment
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page, container, false);

        ContentResolver contentResolver = getContext().getContentResolver();
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        views = new ArrayList<>();
        buttonAdminMainPage = view.findViewById(R.id.admin_button);
        buttonAdminMainPage.setVisibility(View.INVISIBLE);
        buttonViewProfile = view.findViewById(R.id.view_user_button);

        buttonOrganizeEvent = view.findViewById(R.id.organize_event_button);
        buttonBrowseEvent = view.findViewById(R.id.browse_events_button);
        buttonScanQR = view.findViewById(R.id.scan_qr_button);
        viewUserCard = view.findViewById(R.id.view_user_card);
        carouselCardView = view.findViewById(R.id.carouselCardView);
        upComingEvent = view.findViewById(R.id.admin_text);
        progressBar = view.findViewById(R.id.loadingProgressBar);
        carouselProgressBar = view.findViewById(R.id.progressBar);


        views.add(buttonOrganizeEvent);
        views.add(buttonBrowseEvent);
        views.add(buttonScanQR);
        views.add(viewUserCard);
        views.add(carouselCardView);
        views.add(upComingEvent);

        progressBar = view.findViewById(R.id.loadingProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        for (View view1: views) {
            view1.setVisibility(View.INVISIBLE);
        }

        updateProfilePicture();
        eventImages = new ArrayList<>();
        getImageUris(new ImageUriCallback() {
            public void onImageUrisLoaded(List<String> imageUris, List<String> eventNames) {
                carouselProgressBar.setVisibility(View.VISIBLE);

                Context context = getContext();
                eventImages.clear();
                eventImages.addAll(imageUris);

                if (context == null) {
                    authenticateUser();
                    return;
                }

                if (getView() == null) {
                    authenticateUser();
                    return;
                }

                RecyclerView recyclerView = getView().findViewById(R.id.recyclerViewCarousel);
                TextView noEventsTextView = getView().findViewById(R.id.noEventsText);

                if (recyclerView == null || noEventsTextView == null) {
                    carouselProgressBar.setVisibility(View.GONE);
                    return;
                }

                if (imageUris.isEmpty()) {
                    noEventsTextView.setVisibility(View.VISIBLE);
                } else {
                    noEventsTextView.setVisibility(View.INVISIBLE);
                }

                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
                recyclerView.setLayoutManager(layoutManager);

                ImageCarouselAdapter adapter = new ImageCarouselAdapter(context, imageUris, eventNames);
                adapter.setOnItemClickListener(new ImageCarouselAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(String imageUri) {
                        String uriComponents[] = Uri.parse(imageUri).getPath().split("/");
                        String eventId = uriComponents[uriComponents.length - 1];
                        Log.d("clicked event id", eventId);
                        Intent intent = new Intent(getActivity(), BrowseEventsActivity.class);
                        intent.putExtra("eventID", eventId);
                        startActivity(intent);
                    }
                });
                recyclerView.setAdapter(adapter);

                if (!isSnapHelperAttached) {
                    PagerSnapHelper snapHelper = new PagerSnapHelper();
                    snapHelper.attachToRecyclerView(recyclerView);
                    isSnapHelperAttached = true;
                }

                carouselProgressBar.setVisibility(View.INVISIBLE);
            }

        });
        authenticateUser();

        buttonAdminMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_mainPageFragment_to_adminModeMainPageFragment);
            }
        });
        buttonOrganizeEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OrganizeAnEventActivity.class);
                startActivity(intent);
            }
        });
        buttonBrowseEvent.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), BrowseEventsActivity.class);
                startActivity(intent);
            }
        }));
        buttonScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ScanQRActivity.class);
                intent.putExtra("androidId", androidId);
                startActivity(intent);
            }
        });
        buttonViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserInfoActivity.class);
                intent.putExtra("androidId", androidId);
                intent.putExtra("showSwitches", true);
                startActivity(intent);
            }
        });
        return view;
    }

    /**
     * populate the imageView in the top right corner
     */
    private void updateProfilePicture() {
        FirebaseController.getInstance().getUser(androidId, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                if (user != null && user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    Glide.with(getContext())
                            .load(user.getProfilePicture())
                            .circleCrop()
                            .into(buttonViewProfile);
                } else {
                    buttonViewProfile.setImageResource(R.drawable.profile_pic);
                }
            }
        });
}
}

/**
 * adapter for the carousel to fill in the necesary details
 */
class ImageCarouselAdapter extends RecyclerView.Adapter<ImageCarouselAdapter.ViewHolder> {
    private List<String> imageUris;
    private List<String> eventNames;
    private Context context;
    private ImageCarouselAdapter.OnItemClickListener listener;

    public ImageCarouselAdapter(Context context, List<String> imageUris, List<String> eventNames) {
        this.context = context;
        this.imageUris = imageUris;
        this.eventNames = eventNames;
    }

    public interface OnItemClickListener {
        void onItemClick(String imageUri);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.carousel_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUri = imageUris.get(position);
        String eventName = eventNames.get(position);

        Glide.with(context)
                .load(imageUri)
                .into(holder.imageView);

        holder.eventNameTextView.setText(eventName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(imageUri);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView eventNameTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        }
    }
}

