package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import in.testpress.testpress.R;

public class StartExamFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_start_exam, container, false);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle("Exam");
        ((ActionBarActivity)getActivity()).getSupportActionBar().setIcon(R.drawable.ic_home);
        return view;
    }
}
