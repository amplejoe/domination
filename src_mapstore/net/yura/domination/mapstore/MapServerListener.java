package net.yura.domination.mapstore;

import java.util.List;

/**
 * @author Yura Mamyrin
 */
public interface MapServerListener {

    public void gotResultCategories(String url, List categories);
    public void gotResultMaps(String url, List maps);
    public void onXMLError(String string);

    public void downloadFinished(String mapUID);
    public void onDownloadError(String string); // (map file download errors)

    public void publishImg(Object key); // image has been set to the icon for a Map/Category
}
