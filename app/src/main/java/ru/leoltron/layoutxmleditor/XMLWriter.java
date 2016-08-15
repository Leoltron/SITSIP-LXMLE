package ru.leoltron.layoutxmleditor;

import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class XMLWriter {
    private static final String namespace = "http://schemas.android.com/apk/res/android";

    public static String toXMLString() throws IOException {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        xmlSerializer.setOutput(writer);
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.setPrefix("android",namespace);

        for(int i=0; i < MainActivity.layoutContainer.getChildCount(); i++)
            writeViewToXmlSerializer(MainActivity.layoutContainer.getChildAt(i),xmlSerializer);

        xmlSerializer.endDocument();

        return writer.toString();
    }

    /**
     * Writes all view data into XmlSerializer including name, attributes and child information, if exist
     */
    private static void writeViewToXmlSerializer(View view, XmlSerializer xs) throws IOException {
        if(view instanceof IRawAttributesContainer){
            IRawAttributesContainer container = (IRawAttributesContainer)view;
            xs.startTag(null,container.getName());
            putAttributes(container,xs);
            if(view instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) view;
                for (int i = 0; i < parent.getChildCount(); i++) {
                    writeViewToXmlSerializer(parent.getChildAt(i),xs);
                }
            }
            xs.endTag(null,container.getName());
        }
    }

    /**
     * Writes IRawAttributesContainer attributes into XmlSerializer
     */
    public static void putAttributes(IRawAttributesContainer container,XmlSerializer xs) throws IOException {
        Map<String,String> map = container.getRawAttributesMap();
        for (Map.Entry<String,String> a:map.entrySet())
            if(a.getValue().length() > 0)
                xs.attribute(namespace,a.getKey(),a.getValue());
    }
}
