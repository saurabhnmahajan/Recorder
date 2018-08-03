package com.nearby.recorder;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;


public class RecordingsListFragment extends Fragment {

    ListView myRecordingsView;

    private OnFragmentInteractionListener mListener;

    public RecordingsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_recordings_list, container, false);
        myRecordingsView = v.findViewById(R.id.myRecordingsView);
        showRecordings();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void showRecordings() {
        String path = Environment.getExternalStorageDirectory().toString()+"/My Recordings/";

        ArrayList<String > recordingList = new ArrayList<>();
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                recordingList.add(file.getName());
            }
        }
        ArrayAdapter<String> recordingAdapter = new ArrayAdapter<>(Objects.requireNonNull(getActivity()),
                android.R.layout.simple_list_item_1, recordingList);
        myRecordingsView.setAdapter(recordingAdapter);
        myRecordingsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }
}
