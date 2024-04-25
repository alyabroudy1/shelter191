package net.typeblog.shelter.omerflex.servers;

import net.typeblog.shelter.omerflex.entity.Artist;

public interface ControllableServer {

    /**
     * Search for the query and add the result to artistList
     * @param query name to search for
     */
    public void search(String query);

    /**
     * fetch a url and return its items
     * @param artist Artist object to fetch its url
     */
    public void fetch(Artist artist);

    /**
     * fetch a url and return its group item
     * @param artist Artist object to fetch its url if it's a series of group
     */
    public void fetchGroupOfGroup(Artist artist);

    /**
     * fetch a url and return its episode items
     * @param artist Artist object to fetch its url if it's a group of episodes
     */
    public void fetchGroup(Artist artist);

    /**
     * fetch a url and return its serverLinks or resolution links
     * @param artist Artist object to fetch its url
     */
    public void fetchItem(Artist artist);

    /**
     * fetch a url and return its serverLinks
     * @param artist Artist object to fetch its url
     */
    public void fetchServerList(Artist artist);

    /**
     * fetch a url and return its resolution links
     * @param artist Artist object to fetch its url
     */
    public void fetchResolutions(Artist artist);

    /**
     * starts a video intent
     * @param url String url to run video intent
     */
    public void startVideo(String url);

    /**
     * starts a browserActivity
     * @param url String url of the video
     */
    public void startBrowser(String url);

    /**
     * check if artist url an item or group
     * @param artist Artist object to check its url
     * @return true if is series link
     */
    public boolean isSeries(Artist artist);
}
