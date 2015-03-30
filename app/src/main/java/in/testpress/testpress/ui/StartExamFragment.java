package in.testpress.testpress.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
        getActivity().getActionBar().setTitle("Exam");
        getActivity().getActionBar().setIcon(R.drawable.ic_home);
        return view;
    }
}
