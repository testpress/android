package in.testpress.testpress.core;

import in.testpress.course.network.TestpressCourseApiClient;
import in.testpress.models.greendao.Course;

public class CoursePager extends in.testpress.course.network.CoursePager {

    public CoursePager(TestpressCourseApiClient apiClient) {
        super(apiClient);
    }

    @Override
    protected Course register(Course course) {
        if (course != null && !course.getActive()) {
            return null;
        }
        return super.register(course);
    }
}
