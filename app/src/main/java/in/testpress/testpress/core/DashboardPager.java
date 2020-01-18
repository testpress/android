package in.testpress.testpress.core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import in.testpress.testpress.models.DashboardSection;
import in.testpress.testpress.network.DashboardResponse;

public class DashboardPager extends ResourcePager<DashboardSection>  {
    TestpressService service;
    DashboardResponse response = null;

    public DashboardPager(TestpressService service) {
        super(service);
        this.service = service;
    }

    public DashboardResponse getResponse() {
        return response;
    }

    @Override
    public List<DashboardSection> getItems(int page, int size) {
        response = service.getDashboardData();
        return response.getDashboardSections();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    protected Object getId(DashboardSection resource) {
        return resource.getSlug();
    }

}
