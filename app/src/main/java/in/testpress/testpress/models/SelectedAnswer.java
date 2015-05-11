package in.testpress.testpress.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "SelectedAnswer")
public class SelectedAnswer extends Model {
    public SelectedAnswer() {
        super();
    }
    @Column(name = "selectedAnswer")
    public Integer selectedAnswer;
    @Column(name = "ReviewItem", onDelete = Column.ForeignKeyAction.CASCADE, onUpdate = Column.ForeignKeyAction.CASCADE)
    public ReviewItem reviewItem;
    @Column(name = "filter", onDelete = Column.ForeignKeyAction.CASCADE)
    public String filter;
}
