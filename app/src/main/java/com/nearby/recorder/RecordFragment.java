package com.nearby.recorder;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RecordFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    ImageView imageView, pauseView;
    MediaRecorder mediaRecorder;
    Chronometer chronometer;
    Timer timer = null;
    int maxAmps = 0;
    private LinearLayout mainLayout;
    private BarChart mChart;
    List<BarEntry> yVals = new ArrayList<>();

    public RecordFragment() {
        // Required empty public constructor
    }

    public static RecordFragment newInstance(String param1, String param2) {
        RecordFragment fragment = new RecordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v =  inflater.inflate(R.layout.fragment_record, container, false);
        imageView =  v.findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        pauseView = v.findViewById(R.id.pause);
        pauseView.setOnClickListener(this);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                audio_graph(v);
            }
        });
        return v;
    }
    // TODO: Rename method, update argument and hook method into UI event

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public  class AsyncTimer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try{
                        int amps = mediaRecorder.getMaxAmplitude();
                        if(amps > maxAmps)
                            maxAmps = amps;
                        addEntry(amps);
                    }
                    catch (Exception e) {
                        Log.e("Amplitude", "Exception");
                    }
                }
            },0,200);
            return null;
        }
    }
    long timeWhenStopped = 0;
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.imageView:
                if(imageView.getTag().toString().equals("1")) {
                    new AsyncTimer().execute();
                    startRecording();
                }
                else if(imageView.getTag().toString().equals("2")){
                    stopRecording();
                }
                break;
            case R.id.pause:
                if(pauseView.getTag().toString().equals("1")) {
                    //pause
                    timer.cancel();
                    mediaRecorder.pause();
                    timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                    chronometer.stop();
                    pauseView.setTag("2");
                    pauseView.setImageResource(R.drawable.record);
                }
                else if(pauseView.getTag().toString().equals("2")){
                    //resume
                    pauseView.setTag("1");
                    pauseView.setImageResource(R.drawable.pause);
                    chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chronometer.start();
                    new AsyncTimer().execute();
                    mediaRecorder.resume();
                }
                break;

        }
    }
    public void startRecording(){
        yVals.clear();
        File audioFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                "My Recordings");
        if (!audioFolder.exists()) {
            audioFolder.mkdir();
        }
        String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/My Recordings/";
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:ms").format(new Date());
        mFileName +=  timeStamp +  ".mp3";
        mediaRecorder = new MediaRecorder();
        chronometer = getView().findViewById(R.id.chronometer);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(mFileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setAudioChannels(1);
        mediaRecorder.setAudioEncodingBitRate(320000);
        mediaRecorder.setAudioSamplingRate(44100);

        mChart.clear();
        mChart.setData(new BarData());
        imageView.setTag("2");
        imageView.setImageResource(R.drawable.stop);
        pauseView.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            chronometer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording(){
        timer.cancel();
        pauseView.setVisibility(View.INVISIBLE);
        try {
            mediaRecorder.stop();
        }
        catch (Exception e) {
            Log.d("Stop", "exception");
        }
        chronometer.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;

        imageView.setTag("1");
        imageView.setImageResource(R.drawable.record);
        pauseView.setTag("1");
        pauseView.setImageResource(R.drawable.pause);
    }

    public void audio_graph(View v) {
        mainLayout = v.findViewById(R.id.mainLayout);
        mChart = new BarChart(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        mainLayout.addView(mChart, 0, params);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        mChart.getLayoutParams().height = height / 2;
        mChart.setDescription("");
        mChart.setNoDataTextDescription("");
        mChart.setHighlightPerTapEnabled(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setPinchZoom(true);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.getAxisLeft().setDrawLabels(false);

        Legend l = mChart.getLegend();
        l.setEnabled(false);

        XAxis x1 = mChart.getXAxis();
        x1.setTextColor(Color.WHITE);
        x1.setDrawGridLines(false);
        x1.setAvoidFirstLastClipping(true);

        YAxis y1 = mChart.getAxisLeft();
        y1.setTextSize(0f);
        y1.setAxisMaxValue(0);
        y1.setAxisMinValue(0);
        y1.setDrawGridLines(false);

        YAxis y12= mChart.getAxisRight();
        y12.setEnabled(false);

        mChart.setData(new BarData());
    }

    int x = 0;
    private void addEntry(int n) {
        BarData data = mChart.getData();
        if(data!=null) {
            BarDataSet set = (BarDataSet) data.getDataSetByIndex(0);
            if(set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addXValue("");
            new BarEntry(n, -1* n, n);
            YAxis y1 = mChart.getAxisLeft();
            y1.setAxisMaxValue(maxAmps);
            y1.setAxisMinValue(-1*maxAmps);

            yVals.add(new BarEntry(n, set.getEntryCount()/2));
            yVals.add(new BarEntry(-1*n, set.getEntryCount()/2));
            x++;
            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMinimum(50);
            mChart.setVisibleXRangeMaximum(50);
            mChart.moveViewToX(data.getXValCount() - 51);
        }
    }

    private BarDataSet createSet() {
        BarDataSet set = new BarDataSet(yVals, null);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setBarSpacePercent(0f);
        set.setValueTextSize(0);
        return set;
    }
}
