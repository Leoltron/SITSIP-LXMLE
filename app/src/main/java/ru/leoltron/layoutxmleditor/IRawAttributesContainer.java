package ru.leoltron.layoutxmleditor;

import java.util.Map;

public interface IRawAttributesContainer {
    String getName();
    Map<String,String> getRawAttributesMap();
    void setRawAttributesMap(Map<String,String> list);
}
