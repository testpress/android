package in.testpress.testpress.models;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FeedExam {
        private String totalMarks;
        private String url;
        @Id
        private Long id;
        private String title;
        private String duration;
        private Integer numberOfQuestions;
        private Integer studentsAttemptedCount;
        @Generated(hash = 702060980)
        public FeedExam(String totalMarks, String url, Long id, String title,
                String duration, Integer numberOfQuestions,
                Integer studentsAttemptedCount) {
            this.totalMarks = totalMarks;
            this.url = url;
            this.id = id;
            this.title = title;
            this.duration = duration;
            this.numberOfQuestions = numberOfQuestions;
            this.studentsAttemptedCount = studentsAttemptedCount;
        }
        @Generated(hash = 926814208)
        public FeedExam() {
        }
        public String getTotalMarks() {
            return this.totalMarks;
        }
        public void setTotalMarks(String totalMarks) {
            this.totalMarks = totalMarks;
        }
        public String getUrl() {
            return this.url;
        }
        public void setUrl(String url) {
            this.url = url;
        }
        public Long getId() {
            return this.id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getTitle() {
            return this.title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public String getDuration() {
            return this.duration;
        }
        public void setDuration(String duration) {
            this.duration = duration;
        }
        public Integer getNumberOfQuestions() {
            return this.numberOfQuestions;
        }
        public void setNumberOfQuestions(Integer numberOfQuestions) {
            this.numberOfQuestions = numberOfQuestions;
        }
        public Integer getStudentsAttemptedCount() {
            return this.studentsAttemptedCount;
        }
        public void setStudentsAttemptedCount(Integer studentsAttemptedCount) {
            this.studentsAttemptedCount = studentsAttemptedCount;
        }
        }
