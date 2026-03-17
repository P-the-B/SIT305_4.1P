package com.example.eventplanner.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.eventplanner.R;
import com.example.eventplanner.data.Event;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// bridges the event list data to the RecyclerView cards
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // lets fragment handle clicks rather than adapter
    public interface OnEventClickListener {
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    private List<Event> eventList;
    private final OnEventClickListener listener;

    // locale makes sure date format matches the user's region
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE dd MMM yyyy  hh:mm a", Locale.getDefault());

    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    // swaps in new data and re-prints the list
    public void setEvents(List<Event> events) {
        this.eventList = events;
        notifyDataSetChanged();
    }

    // inflates the card layout for each new visible row
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    // populates each card with real event data
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.textTitle.setText(event.title);
        holder.textCategory.setText(event.category);
        holder.textLocation.setText(event.location);
        holder.textDateTime.setText(dateFormat.format(new Date(event.dateTimeMillis)));

        holder.itemView.setOnClickListener(v -> listener.onEditClick(event));
        holder.buttonDelete.setOnClickListener(v -> listener.onDeleteClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    // holds references to views in each card so we're not calling findViewById repeatedly
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textCategory, textLocation, textDateTime;
        ImageButton buttonDelete;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
            textCategory = itemView.findViewById(R.id.textCategory);
            textLocation = itemView.findViewById(R.id.textLocation);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}