package in.testpress.testpress.network;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import in.testpress.testpress.models.RssFeed;
import in.testpress.testpress.models.RssItem;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * A {@link Converter converter} which uses {@link XMLParser} to parse RSS feeds.
 */

public final class RssConverterFactory implements Converter {

    public static RssConverterFactory create() {
        return new RssConverterFactory();
    }

    private RssConverterFactory() {
        super();
    }

    @Override
    public Object fromBody(TypedInput body, Type type) {
        RssFeed rssFeed = new RssFeed();
        try {
            XMLParser parser = new XMLParser();
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = parserFactory.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(parser);
            InputSource inputSource = new InputSource(body.in());
            xmlReader.parse(inputSource);
            ArrayList<RssItem> items = parser.getItems();
            rssFeed.setItems(items);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rssFeed;
    }

    @Override
    public TypedOutput toBody(Object object) {
        return null;
    }
}
