package in.testpress.testpress.util;

import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html.TagHandler;

public class ListTagHandler implements TagHandler {
    boolean first= true;
    String parent=null;
    int index=1;

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {

        if(tag.equals("ul")) {
            parent = "ul";
        } else if(tag.equals("ol")) {
            parent = "ol";
        }
        if(tag.equals("li")) {
            if(parent.equals("ul")) {
                if(first) {
                    output.append("\n\t• ");
                    first = false;
                } else {
                    first = true;
                }
            }
            else {
                if(first) {
                    output.append("\n\t" + index + ". ");
                    first = false;
                    index++;
                } else {
                    first = true;
                }
            }
        }
    }
}
