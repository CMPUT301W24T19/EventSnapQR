package com.example.eventsnapqr;

import android.content.Context;
import android.os.Build;
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
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminBrowseEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminBrowseEventsFragment extends Fragment implements FirebaseController.OnEventsLoadedListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FloatingActionButton buttonBackToAdminMain;

    public AdminBrowseEventsFragment() {
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
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private ListView listView;
    private FirebaseController firebaseController;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }
    private Button searchBtn;
    private ArrayList<Event> eventsDataList;
    private EventAdapter eventAdapter;
    private EditText editTextSearch;
    private ArrayList<Event> viewList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_events, container, false);
        searchBtn = view.findViewById(R.id.button_search);
        editTextSearch = view.findViewById(R.id.search_bar);
        viewList = new ArrayList<>();
        FirebaseController.OnEventsLoadedListener controllerRef = this::onEventsLoaded;
        searchBtn.setOnClickListener(new View.OnClickListener() {
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
        firebaseController = new FirebaseController();
        firebaseController.getEvents(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                firebaseController.deleteEvent(eventsDataList.get(position));
                eventsDataList.remove(position);
                eventAdapter.notifyDataSetChanged();
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

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onEventsLoaded(ArrayList<Event> events) {
        this.eventsDataList = events;
        eventAdapter = new EventAdapter(getContext(), eventsDataList);
        listView.setAdapter(eventAdapter);
        eventAdapter.notifyDataSetChanged();
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