package in.testpress.testpress.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.ListView;

import com.github.kevinsawicki.wishlist.SingleTypeAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import in.testpress.testpress.Injector;
import in.testpress.testpress.R;
import in.testpress.testpress.core.TestpressService;
import in.testpress.testpress.models.Form;

public class ApplicationFormFragment extends ItemListFragment<Form> {

    @Inject TestpressService testpressService;

    List<Form> forms = Collections.emptyList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.inject(this);
        setHasOptionsMenu(false);
    }

    @Override
    protected void configureList(Activity activity, ListView listView) {
        super.configureList(activity, listView);
        listView.setDividerHeight(0);
    }

    @Override
    protected int getErrorMessage(Exception exception) {
        return R.string.network_error;
    }

    @Override
    protected SingleTypeAdapter<Form> createAdapter(List<Form> items) {
        return new FormListAdapter(getActivity(), testpressService, items);
    }

    @Override
    public Loader<List<Form>> onCreateLoader(int id, Bundle args) {
        return new FormsLoader(this, forms);
    }

    private static class FormsLoader extends ThrowableLoader<List<Form>> {

        private ApplicationFormFragment fragment;

        FormsLoader(ApplicationFormFragment fragment, List<Form> forms) {
            //noinspection ConstantConditions
            super(fragment.getContext(), forms);
            this.fragment = fragment;
        }

        @Override
        public List<Form> loadData() throws Exception {
            HashMap<Integer, Form> json = fragment.testpressService.getForms();
            ArrayList<Form> forms = new ArrayList<>(json.values());
            ArrayList<Form> orderedForms = new ArrayList<>();
            ArrayList<Form> openForms = new ArrayList<>();
            ArrayList<Form> upcomingForms = new ArrayList<>();
            ArrayList<Form> closedForms = new ArrayList<>();
            ArrayList<Form> miscellaneousForms = new ArrayList<>();
            for (Form form : forms) {
                switch (form.getStatus()) {
                    case "1":
                        openForms.add(form);
                        break;
                    case "2":
                        upcomingForms.add(form);
                        break;
                    case "3":
                        closedForms.add(form);
                        break;
                    default:
                        miscellaneousForms.add(form);
                        break;
                }
            }
            orderedForms.addAll(openForms);
            orderedForms.addAll(upcomingForms);
            orderedForms.addAll(closedForms);
            orderedForms.addAll(miscellaneousForms);
            return orderedForms;
        }
    }

}
