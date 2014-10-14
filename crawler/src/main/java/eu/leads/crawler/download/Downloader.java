package eu.leads.crawler.download;

import eu.leads.crawler.model.Page;

import java.net.URL;

/**
 * Page downloader
 *
 * @author ameshkov
 */
public interface Downloader {

    /**
     * Downloads specified page
     *
     * @param url
     *
     * @return
     */
    Page download(URL url) throws DownloadException;
}
