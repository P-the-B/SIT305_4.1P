package com.example.eventplanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
import com.example.eventplanner.adapter.EventAdapter;
import com.example.eventplanner.data.Event;
import com.example.eventplanner.data.EventDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

// home screen - shows all events as a sorted list
public class EventListFragment extends Fragment {

    private EventAdapter adapter;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        FloatingActionButton fab = view.findViewById(R.id.fabAddEvent);

        adapter = new EventAdapter(new ArrayList<>(), new EventAdapter.OnEventClickListener() {
            // pass the event ID to the edit form via nav args
            @Override
            public void onEditClick(Event event) {
                Bundle args = new Bundle();
                args.putInt("eventId", event.id);
                Navigation.findNavController(view)
                        .navigate(R.id.addEditEventFragment, args);
            }
            // confirm before deleting - runs delete on background thread
            @Override
            public void onDeleteClick(Event event) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete this event? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            new Thread(() -> {
                                EventDatabase.getInstance(requireContext()).eventDao().delete(event);
                                requireActivity().runOnUiThread(() -> {
                                    loadEvents(view);
                                    Snackbar.make(view, "Event deleted", Snackbar.LENGTH_SHORT).show();
                                });
                            }).start();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.addEditEventFragment));

        loadEvents(view);
    }
    // reload list every time
    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) loadEvents(getView());
    }
    // Room can't run on the main thread so we use a background thread, then post results back to UI
    private void loadEvents(View view) {
        new Thread(() -> {
            List<Event> events = EventDatabase.getInstance(requireContext())
                    .eventDao().getAllEventsSortedByDate();
            requireActivity().runOnUiThread(() -> {
                adapter.setEvents(events);
                emptyView.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }
}