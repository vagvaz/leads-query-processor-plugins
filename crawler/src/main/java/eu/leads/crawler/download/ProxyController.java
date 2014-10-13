package eu.leads.crawler.download;

import java.net.Proxy;

/**
 * Manages proxies list.
 *
 * @author ameshkov
 */
public interface ProxyController {

    /**
     * Returns proxy
     *
     * @return
     */
    Proxy getProxy();
}
