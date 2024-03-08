package com.example.eventsnapqr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminBrowseEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminBrowseEventFragment extends Fragment implements FirebaseController.OnEventsLoadedListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private FloatingActionButton buttonBackToAdminMain;

    public AdminBrowseEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminBrowseEvents.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminBrowseEventsFragment newInstance(String param1, String param2) {
        AdminBrowseEventsFragment fragment = new AdminBrowseEventsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    private ListView listView;
    private FirebaseController firebaseController;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    private Button searchButton;
    private ArrayList<Event> eventsDataList;
    private EventAdapter eventAdapter;
    private EditText editTextSearch;
    private ArrayList<Event> viewList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_event, container, false);
        searchButton = view.findViewById(R.id.button_search);
        editTextSearch = view.findViewById(R.id.search_bar);

        viewList = new ArrayList<>();
        FirebaseController.OnEventsLoadedListener controllerRef = this::onEventsLoaded;
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewList.clear();
                for(Event e: eventsDataList){
                    if(e.getQrCode().getLink().contains(editTextSearch.getText())){
                        viewList.add(e);
                    }
                }
                if(!viewList.isEmpty()){
                    eventAdapter = new EventAdapter(getContext(),viewList);
                    listView.setAdapter(eventAdapter);

                }
            }
        });
        listView = view.findViewById(R.id.events);
        final ProgressBar progressBar = view.findViewById(R.id.progress_bar); // Make sure you have a ProgressBar in your layout
        progressBar.setVisibility(View.VISIBLE);
        firebaseController = new FirebaseController();
        firebaseController.getAllEvents(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Delete Event");
                Event event = eventsDataList.get(position);
                builder.setMessage("Are you sure you want to delete the event: " + event.getEventName() + "?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked Yes, delete the event
                        Runnable completionCallback = null;
                        firebaseController.deleteEvent(event, (FirebaseController.FirestoreOperationCallback) completionCallback);
                        eventsDataList.remove(position);
                        eventAdapter.notifyDataSetChanged();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        buttonBackToAdminMain = view.findViewById(R.id.button_back_button);
        buttonBackToAdminMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_adminBrowseEventsFragment_to_AdminModeMainPageFragment);
            }
        });

        return view;
    }

    @Override
    public void onEventsLoaded(ArrayList<Event> events) {

        if (getView() != null) {
            final ProgressBar progressBar = getView().findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);
        }

        this.eventsDataList = events;
        eventAdapter = new EventAdapter(getContext(), eventsDataList);
        listView.setAdapter(eventAdapter);
    }
}
class EventAdapter extends ArrayAdapter<Event> {

    private LayoutInflater inflater;
    private ArrayList<Event> events;
    private Context context;
    public EventAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.events = events;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_event, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.eventNameTextView = convertView.findViewById(R.id.textview_event_name);
            viewHolder.qrLinkTextView = convertView.findViewById(R.id.textview_qr_link);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Get the event at the specified position
        Event event = getItem(position);
        if (event != null) {
            viewHolder.eventNameTextView.setText(event.getEventName());
            // Check if organizer is not null before accessing its properties
            if (event.getOrganizer() != null) {
                viewHolder.qrLinkTextView.setText(event.getQrCode().getLink());
            } else {
                viewHolder.qrLinkTextView.setText("Unknown link");
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView eventNameTextView;
        TextView qrLinkTextView;
        // Add more views if needed
    }
}