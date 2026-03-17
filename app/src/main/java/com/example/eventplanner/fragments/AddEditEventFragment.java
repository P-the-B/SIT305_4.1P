package com.example.eventplanner.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.eventplanner.R;
import com.example.eventplanner.data.Event;
import com.example.eventplanner.data.EventDatabase;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// handles both add and edit in one fragment - mode is determined by whether eventId was passed in
public class AddEditEventFragment extends Fragment {

    private TextInputLayout layoutTitle, layoutDateTime;
    private TextInputEditText editTitle, editLocation, editDateTime;
    private Spinner spinnerCategory;
    private Button buttonDelete;

    private Calendar selectedDateTime = null;
    private int editingEventId = -1; // -1 means we're adding, anything else means editing

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("EEE dd MMM yyyy  hh:mm a", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutTitle = view.findViewById(R.id.layoutTitle);
        layoutDateTime = view.findViewById(R.id.layoutDateTime);
        editTitle = view.findViewById(R.id.editTitle);
        editLocation = view.findViewById(R.id.editLocation);
        editDateTime = view.findViewById(R.id.editDateTime);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonDelete = view.findViewById(R.id.buttonDelete);

        // populate category dropdown from strings.xml array
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.event_categories,
                android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // check if an event ID was passed in from the list screen
        if (getArguments() != null) {
            editingEventId = getArguments().getInt("eventId", -1);
        }

        // if editing, load existing data and show the delete button
        if (editingEventId != -1) {
            loadExistingEvent();
            buttonDelete.setVisibility(View.VISIBLE);
        }

        editDateTime.setOnClickListener(v -> showDatePicker());
        buttonSave.setOnClickListener(v -> saveEvent(view));
        buttonCancel.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        buttonDelete.setOnClickListener(v -> confirmDelete(view));
    }

    // fetch the event from DB on background thread, then fill the form on UI thread
    private void loadExistingEvent() {
        new Thread(() -> {
            Event event = EventDatabase.getInstance(requireContext())
                    .eventDao().getEventById(editingEventId);
            requireActivity().runOnUiThread(() -> {
                if (event != null) {
                    editTitle.setText(event.title);
                    editLocation.setText(event.location);

                    // match saved category back to spinner position
                    String[] categories = getResources().getStringArray(R.array.event_categories);
                    for (int i = 0; i < categories.length; i++) {
                        if (categories[i].equals(event.category)) {
                            spinnerCategory.setSelection(i);
                            break;
                        }
                    }

                    selectedDateTime = Calendar.getInstance();
                    selectedDateTime.setTimeInMillis(event.dateTimeMillis);
                    editDateTime.setText(dateFormat.format(new Date(event.dateTimeMillis)));
                }
            });
        }).start();
    }

    // date picker chains into time picker - validates immediately on selection
    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
            new TimePickerDialog(requireContext(), (timePicker, hour, minute) -> {
                selectedDateTime = Calendar.getInstance();
                selectedDateTime.set(year, month, day, hour, minute, 0);
                selectedDateTime.set(Calendar.MILLISECOND, 0);

                // show inline error immediately if past date selected
                if (selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
                    layoutDateTime.setError("Date cannot be in the past");
                    editDateTime.setText("");
                    selectedDateTime = null;
                } else {
                    layoutDateTime.setError(null);
                    editDateTime.setText(dateFormat.format(selectedDateTime.getTime()));
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    // always confirm before deleting - safety net for accidental taps
    private void confirmDelete(View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        Event event = EventDatabase.getInstance(requireContext())
                                .eventDao().getEventById(editingEventId);
                        if (event != null) {
                            EventDatabase.getInstance(requireContext()).eventDao().delete(event);
                        }
                        requireActivity().runOnUiThread(() -> {
                            Snackbar.make(view, "Event deleted", Snackbar.LENGTH_SHORT).show();
                            Navigation.findNavController(view).navigateUp();
                        });
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveEvent(View view) {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";
        String location = editLocation.getText() != null ? editLocation.getText().toString().trim() : "";
        String category = spinnerCategory.getSelectedItem().toString();

        // validate title
        if (title.isEmpty()) {
            layoutTitle.setError("Title is required");
            return;
        } else {
            layoutTitle.setError(null);
        }

        // validate date picked
        if (selectedDateTime == null) {
            layoutDateTime.setError("Please select a date and time");
            return;
        }

        // validate date not in past
        if (selectedDateTime.getTimeInMillis() < System.currentTimeMillis()) {
            layoutDateTime.setError("Date cannot be in the past");
            return;
        }

        layoutDateTime.setError(null);
        long dateTimeMillis = selectedDateTime.getTimeInMillis();

        // insert or update depending on mode, always on background thread
        new Thread(() -> {
            if (editingEventId == -1) {
                Event newEvent = new Event(title, category, location, dateTimeMillis);
                EventDatabase.getInstance(requireContext()).eventDao().insert(newEvent);
            } else {
                Event updatedEvent = new Event(title, category, location, dateTimeMillis);
                updatedEvent.id = editingEventId;
                EventDatabase.getInstance(requireContext()).eventDao().update(updatedEvent);
            }
            requireActivity().runOnUiThread(() -> {
                Snackbar.make(view, editingEventId == -1 ? "Event added!" : "Event updated!",
                        Snackbar.LENGTH_SHORT).show();
                Navigation.findNavController(view).navigateUp();
            });
        }).start();
    }
}