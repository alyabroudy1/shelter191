package net.typeblog.shelter.omerflex.servers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import net.typeblog.shelter.R;

import net.typeblog.shelter.omerflex.entity.Artist;
import net.typeblog.shelter.omerflex.entity.ArtistList;
import net.typeblog.shelter.omerflex.ui.GroupActivity;
import net.typeblog.shelter.omerflex.ui.ItemActivity;
import net.typeblog.shelter.omerflex.ui.ResolutionsListActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * from SearchActivity or MainActivity -> if item -> resolutionsActivity
 * if series -> groupActivity -> resolutionsActivity
 * -> if security check -> web browser intent
 * -> else to video intent
 */
public class AkwamController implements ControllableServer {

    List<Artist> artistList;
    TextView descriptionTextView;
    static String TAG = "Akwam";

    Activity activity;
    ArtistList adapter;
    static boolean START_BROWSER_CODE = false;

    public AkwamController(List<Artist> artistList, Activity activity, ArtistList adapter) {
        this.artistList = artistList;
        this.activity = activity;
        this.adapter = adapter;
    }

    @Override
    public void search(String query) {
        Log.i(TAG, "search: " + query);
        switch (query){
            case "https://ak.sv/movies":
                break;
            case "مسلسلات":
                query = "https://ak.sv/series";
                break;
            case "كوميدي":
                query = "https://ak.sv/movies?section=0&category=20&rating=0&year=0&language=0&format=0&quality=0";
                break;
            case "رعب":
                query = "https://ak.sv/movies?section=0&category=22&rating=0&year=0&language=0&format=0&quality=0";
                break;
            default:
                query = "https://ak.sv/search?q=" + query;
        }
        final String url = query;
       Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "Search url:" + url);
                    Document doc = Jsoup.connect(url).header(
                            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                            .userAgent("Mozilla/5.0 (Linux; Android 8.1.0; Android SDK built for x86 Build/OSM1.180201.031; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100 Mobile Safari/537.36")
                            .header(
                            "accept-encoding", "gzip, deflate").header(
                            "accept-language", "en,en-US;q=0.9")
                            //.header("x-requested-with", "pc1")
                            .followRedirects(true).ignoreHttpErrors(true).timeout(6000).get();

                    Log.d(TAG, "run: Response");
                    Elements links = doc.getElementsByClass("entry-box");
                    Log.d(TAG, "run: title: "+doc.title());
                    for (Element link : links) {
                        String linkUrl = link.getElementsByClass("box").attr("href");
                        if (
                                linkUrl.contains("/movie") ||
                                        linkUrl.contains("/series") ||
                                        linkUrl.contains("/episode")
                        ) {
                            Artist a = new Artist();
                            a.setServer(Artist.SERVER_AKWAM);
                            Log.i("link found", linkUrl + "");

                            a.setName(link.getElementsByAttribute("src").attr("alt"));
                            a.setUrl(linkUrl);
                            a.setImage(link.getElementsByAttribute("src").attr("data-src"));
                            String rate = link.getElementsByClass("label rating").text();
                            a.setRate(rate);
                            Log.i("rate found", rate + "");
                            if (isSeries(a)) {
                                a.setState(Artist.GROUP_STATE);
                            } else {
                                a.setState(Artist.ITEM_STATE);
                            }
                            artistList.add(a);
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                } catch (IOException e) {
                    Log.i(TAG, "error: " + e.getMessage());
                }
            }
        });
       t.start();

            Log.i(TAG, "Search End");
    }

    @Override
    public void fetch(Artist artist) {
        Log.i(TAG, "fetch: " + artist.getUrl());
        Intent intent = null;
        if (artist.getState() == Artist.VIDEO_STATE){
            startVideo(artist.getUrl());
        }else {

            switch (artist.getState()) {
                case Artist.GROUP_STATE:
                    Log.i(TAG, "onItemClick. GROUP_STATE" + artist.getServer() + ". url:" + artist.getUrl());
                    intent = new Intent(activity, GroupActivity.class);
                    intent.putExtra("ARTIST_IS_VIDEO", false);
                    break;
                case Artist.ITEM_STATE:
                    Log.i(TAG, "onItemClick. ITEM_STATE " + artist.getServer() + ". url:" + artist.getUrl());
                    intent = new Intent(activity, ItemActivity.class);
                    break;
                case Artist.RESOLUTION_STATE:
                    Log.i(TAG, "onItemClick. RESOLUTION_STATE " + artist.getServer() + ". url:" + artist.getUrl());
                    intent = new Intent(activity, ResolutionsListActivity.class);
                    break;
                default:
                    intent = new Intent(activity, ResolutionsListActivity.class);
            }
            intent.putExtra("ARTIST_URL", artist.getUrl());
            intent.putExtra("ARTIST_NAME", artist.getName());
            intent.putExtra("ARTIST_STATE", artist.getState());
            intent.putExtra("ARTIST_IMAGE", artist.getImage());
            intent.putExtra("ARTIST_SERVER", artist.getServer());
            activity.startActivity(intent);
        }
    }

    @Override
    public void fetchGroupOfGroup(Artist artist) {
        Log.i(TAG, "fetchGroupOfGroup: " + artist.getUrl());

    }

    @Override
    public void fetchGroup(Artist artist) {
        Log.i(TAG, "fetchGroup: " + artist.getUrl());
        final String url = artist.getUrl();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "FetchSeriesLink url:" + url);

                    descriptionTextView = (TextView) activity.findViewById(R.id.textViewDesc);

                    Document doc = Jsoup.connect(url).header(
                            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8").header(
                            "User-Agent", " Mozilla/5.0 (Linux; Android 8.1.0; Android SDK built for x86 Build/OSM1.180201.031; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100 Mobile Safari/537.36").header(
                            "accept-encoding", "gzip, deflate").header(
                            "accept-language", "en,en-US;q=0.9").header(
                            "x-requested-with", "pc1"
                    ).timeout(6000).get();
                    //description
                    Elements decDivs = doc.select("h2");

                    for (Element div : decDivs) {
                        final String description = div.getElementsByTag("p").html();
                        Log.i("description", "found:" + description);

                        if (null != description && !description.equals("")) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    descriptionTextView.setText(description);
                                }
                            });
                            break;
                        }
                    }

                    Elements links = doc.select("a");
                    for (Element link : links) {
                        // TODO: find better way to get the link
                        if (
                                link.attr("href").contains("/episode") &&
                                        link.getElementsByAttribute("src").hasAttr("alt")
                        ) {
                            Artist a = new Artist();
                            a.setServer(Artist.SERVER_AKWAM);
                            Log.i(TAG, "linkFound:" + link.attr("href") + "");

                            a.setName(link.getElementsByAttribute("src").attr("alt"));
                            a.setUrl(link.attr("href"));
                            a.setImage(link.getElementsByAttribute("src").attr("src"));
                            a.setState(Artist.ITEM_STATE);
                            artistList.add(a);
                        }
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Collections.reverse(artistList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    Log.i(TAG + "failed", e.getMessage() + "");
                }
            }
        }).start();
    }

    /**
     * if its an episode then start new resolution activity to fetch episode resolution
     * or in new Akwam only to fetch video link from security check page
     * @param artist Artist object to fetch its url
     */
    @Override
    public void fetchItem(Artist artist) {
        Log.i(TAG, "fetchItem: " + artist.getUrl());
   /*     if (artist.getState() == Artist.RESOLUTION_STATE) {
            Intent fetchResolutionIntent = new Intent(activity, ResolutionsListActivity.class);
            //    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            fetchResolutionIntent.putExtra("ARTIST_STATE", artist.getState()); //very important to start right method in browser activity

            fetchResolutionIntent.putExtra("ARTIST_NAME", artist.getName());
            fetchResolutionIntent.putExtra("ARTIST_SERVER", artist.getServer());
            fetchResolutionIntent.putExtra("ARTIST_IMAGE", artist.getImage());

            fetchResolutionIntent.putExtra("ARTIST_IS_VIDEO", artist.getIsVideo());

            fetchResolutionIntent.putExtra("ARTIST_URL", artist.getUrl());
            activity.startActivity(fetchResolutionIntent);

        } else {  // fetch video link from security check page using browserActivity


    */
        new Thread(new Runnable() {
            @Override
            public void run() {

                descriptionTextView = (TextView) activity.findViewById(R.id.textViewDesc);

                try {
                    String url = artist.getUrl();

                    // page2 fetch goo- links
                    String p2Caption = "/link/";
                    Document doc = Jsoup.connect(url).header(
                            "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8").header(
                            "User-Agent", " Mozilla/5.0 (Linux; Android 8.1.0; Android SDK built for x86 Build/OSM1.180201.031; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100 Mobile Safari/537.36").header(
                            "accept-encoding", "gzip, deflate").header(
                            "accept-language", "en,en-US;q=0.9").header(
                            "x-requested-with", "pc1"
                    ).timeout(6000).get();

                    //description
                    Elements decDivs = doc.select("h2");
                    for (Element div : decDivs) {
                        String description = div.getElementsByTag("p").html();
                        Log.i("description", "found:" + description);

                        if (null != description && !description.equals("")) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    descriptionTextView.setText(description);
                                }
                            });
                            break;
                        }
                    }

                    //TODO: find better way to fetch links
                    Elements divs = doc.getElementsByClass("tab-content quality");
                    for (Element div : divs) {
                        Artist a = new Artist(artist.getId(), "", artist.getGenre(), "", artist.getImage(), artist.getRate(), artist.getServer(), true, "", Artist.VIDEO_STATE);
                        Elements links = div.getElementsByAttribute("href");
                        for (Element link : links) {
                            if (link.attr("href").contains(p2Caption) || link.attr("href").contains("/download/") ) {
                                a.setUrl(link.attr("href"));
                                a.setName(link.text());
                            }
                        }
                        a.setState(Artist.RESOLUTION_STATE);
                        artistList.add(a);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.i(TAG, "error:" + e.getMessage());
                }

            }
        }).start();


     //   }

    }

    @Override
    public void fetchServerList(Artist artist) {
        Log.i(TAG, "fetchServerList: " + artist.getUrl());

    }

    @Override
    public void fetchResolutions(Artist artist) {
        Log.i(TAG, "fetchResolutions: " + artist.getUrl());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = artist.getUrl();
                    Document doc = null;

                    if (url.contains("/link")) {
                        doc = Jsoup.connect(url)
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                .userAgent("Mozilla/5.0 (Linux; Android 8.1.0; Android SDK built for x86 Build/OSM1.180201.031; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100 Mobile Safari/537.36")
                                .followRedirects(true)
                                .ignoreHttpErrors(true)
                                .timeout(0)
                                .ignoreContentType(true)
                                .get();

                        String oldUrl = url;
                        String regex = "(?:a[kwamoc])?.*/[download]{1,6}";

                        Pattern pattern = Pattern.compile(regex);

                        Elements links = doc.getElementsByClass("download-link");
                        for (int i = 0; i < links.size(); i++) {
                            Element link = links.get(i);
                            String pLink = link.attr("href");
                            Matcher matcher = pattern.matcher(pLink);
                            if (matcher.find()) {
                                //Log.d(TAG, "fetchToWatchLocally 2-run: matching1 " + pLink);
                                url = pLink;
                                break;
                            }
                        }
                        if (oldUrl.equals(url)) {
//  Elements links = doc.select("a");
                            links = doc.getElementsByTag("a");
                            //Log.d(TAG, "fetchToWatchLocally run-3: old-url:" + oldUrl);
                            for (int i = 0; i < links.size(); i++) {
                                Element link = links.get(i);
                                String pLink = link.attr("href");
                                Matcher matcher = pattern.matcher(pLink);
                                if (matcher.find()) {
                                    //  databaseMovie.child(movie.getId()).child("url").setValue(link.attr("href"));
                                    //movie.setUrl(link.attr("href"));
                                    //Log.d(TAG, "fetchToWatchLocally 2-run: matching2 " + pLink);
                                    url = pLink;
                                    break;
                                }
                            }
                        }

                        doc = Jsoup.connect(url)
                                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                                .userAgent("Mozilla/5.0 (Linux; Android 8.1.0; Android SDK built for x86 Build/OSM1.180201.031; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100 Mobile Safari/537.36")
                                .followRedirects(true)
                                .ignoreHttpErrors(true)
                                .timeout(0)
                                .ignoreContentType(true)
                                .get();

                        //check if security caption
                        Elements divs = doc.getElementsByClass("btn-loader");
                        Element form = doc.getElementById("form");

                        Elements hs = doc.getElementsByTag("h1");

                        boolean isCheck = divs.size() == 0;
                        Log.i("isCheck", "size:" + isCheck);

                        if (!isCheck) {
                            String videoCaption = "akwam.download";
                            String videoCaption2 = "akwam.link";
                            String videoCaption3 = "/download/";
                            for (Element div : divs) {
                                Elements links2 = div.getElementsByAttribute("href");
                                for (Element link : links2) {
                                    if (link.attr("href").contains(videoCaption)
                                            || link.attr("href").contains(videoCaption2)
                                            || link.attr("href").contains(videoCaption3)
                                    ) {
                                        //  databaseArtist.child(artist.getId()).child("url").setValue(link.attr("href"));
                                        //artist.setUrl(link.attr("href"));
                                        Log.i("akwam url3", link.attr("href"));
                                        url = link.attr("href");
                                        // artist.setUrl(url);
                                    }
                                }
                            }
                            Log.i(TAG, "FetchOneLink url3:" + url);

                            String type = "video/*"; // It works for all video application
                            Uri uri = Uri.parse(url);
                            Intent in1 = new Intent(Intent.ACTION_VIEW, uri);
                            in1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            //  in1.setPackage("org.videolan.vlc");
                            in1.setDataAndType(uri, type);
                            activity.startActivity(in1);
                            activity.finish();

                        } else {
//                            startBrowser(url);
                        }

                    }
                } catch (IOException e) {
                    //builder.append("Error : ").append(e.getMessage()).append("\n");
                    Log.i(TAG, "FetchVideoLink: " + e.getMessage() + "");
                }
            }
        }).start();

        Log.i(TAG, "fetchResolutions: and");

    }

    @Override
    public void startVideo(final String link) {
        Log.i(TAG, "startVideo: " + link);
//        AkwamController.START_BROWSER_CODE = true;
//
//        WebView simpleWebView = (WebView) activity.findViewById(R.id.webView);
//        simpleWebView.clearCache(true);
//        simpleWebView.clearFormData();
//        simpleWebView.clearHistory();
//
//        simpleWebView.setWebViewClient(new Browser_Home() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                Log.d("WEBCLIENT", "OnreDirect url:" + url);
//                if (url.equals(link)){
//                    AkwamController.START_BROWSER_CODE = true;
//                }
//                return !url.contains("akwam.");
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                Log.d("WEBCLIENT", "onPageFinished");
//                if (AkwamController.START_BROWSER_CODE) {
//                    view.evaluateJavascript("(function() { var x = document.getElementsByClassName(\"link btn btn-light\")[0]; return x.getAttribute(\"href\").toString();})();", new ValueCallback<String>() {
//                        @Override
//                        public void onReceiveValue(String s) {
//                            Log.d("LogName", s); // Prints the string 'null' NOT Java null
//                            if (s.contains(".download")) {
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                String type = "video/*"; // It works for all video application
//                                String link = s.replace("\"", "");
//                                Uri uri = Uri.parse(link);
//                                intent.setDataAndType(uri, type);
//                                try {
//                                    activity.startActivity(intent);
//                                } catch (ActivityNotFoundException e) {
//                                    Log.d("errorr", e.getMessage());
//                                }
//                                AkwamController.START_BROWSER_CODE = false;
//                                activity.finish();
//                            }
//                        }
//                    });
//
//                }
//            }
//
//            @Override
//            public void onLoadResource(WebView view, String url) {
//                super.onLoadResource(view, url);
//                Log.d("WEBCLIENT", "onLoadResource");
//
//
//            }
//        });
//        simpleWebView.setWebChromeClient(new ChromeClient());
//        WebSettings webSettings = simpleWebView.getSettings();
//
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setAllowFileAccess(true);
////        webSettings.setAppCacheEnabled(true);
//
//        simpleWebView.loadUrl(link);
    }

    @Override
    public void startBrowser(String url) {
//        Log.i(TAG, "startBrowser: " + url);
//        Intent browserIntent = new Intent(activity, BrowserActivity.class);
//        //    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//       // browserIntent.putExtra("ARTIST_STATE", Artist.ITEM_STATE); //very important to start right method in browser activity
//        browserIntent.putExtra("ARTIST_STATE", Artist.VIDEO_STATE); //very important to start right method in browser activity
//
//        browserIntent.putExtra("ARTIST_NAME", "akwam");
//        browserIntent.putExtra("ARTIST_IMAGE", "artist.getImage()");
//
//        browserIntent.putExtra("ARTIST_IS_VIDEO", true);
//
//        browserIntent.putExtra("ARTIST_URL", url);
//        browserIntent.putExtra("ARTIST_SERVER", Artist.SERVER_AKWAM);
//        Log.i(TAG, "Akwam check url:" + url);
//        activity.startActivity(browserIntent);
    }

    @Override
    public boolean isSeries(Artist artist) {
        String u = artist.getUrl();
        return u.contains("/series") || u.contains("/movies");
    }


    private class Browser_Home extends WebViewClient {
        Browser_Home() {
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            super.shouldOverrideUrlLoading(view, url);
            Log.i("override", "url: " + url);
            //return !url.contains(getServerText(artist.getServer()));
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }
    }

    private class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        ChromeClient() {
        }


        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(activity.getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) activity.getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            activity.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            activity.setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = activity.getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) activity.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            activity.getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    public String getServerText(String serverName) {
        switch (serverName) {
            case Artist.SERVER_SHAHID4U:
                return "https://shahid4u";
            case Artist.SERVER_FASELHD:
                return "www.faselhd";
            case Artist.SERVER_CIMA4U:
                return "cima4u.io/";
            case Artist.SERVER_AKWAM:
                return "akwam.";

        }
        return "https://shahid4u";
    }

}
