package my.mmu.rssnewsreader.service.rss;

import android.util.Log;

import org.unbescape.html.HtmlEscape;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RssHandler extends DefaultHandler {

    private RssFeed rssFeed;
    private RssItem rssItem;
    private StringBuilder stringBuilder;
    private String imageUrl;

    @Override
    public void startDocument() {
        rssFeed = new RssFeed();
    }

    public RssFeed getRssFeed() {
        return rssFeed;
    }


    //Called when an opening tag is reached, such as <item> or <title>
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        stringBuilder = new StringBuilder();

        switch (qName) {
            case "item":
                rssItem = new RssItem();
                break;
            case "media:thumbnail":
            case "media:content":
            case "image":
                if (attributes.getValue("url") != null) {
                    imageUrl = attributes.getValue("url");
                }
                break;
        }
    }

    //Called when a closing tag is reached, such as </item> or </title>
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if ("item".equals(qName)) {
                // End of an item, add the current item to the feed's list of items
                if (rssItem != null) {
                    rssFeed.addRssItem(rssItem);
                    Log.d("RssHandler", "Added RSS item: " + rssItem.getTitle());
                } else {
                    Log.w("RssHandler", "RSS item is null; skipping addition.");
                }
                rssItem = null; // Reset for the next item
            }

            if (rssFeed != null && rssItem == null) { // Parse feed-level properties
                switch (qName) {
                    case "title":
                        rssFeed.setTitle(stringBuilder.toString().trim());
                        break;
                    case "link":
                        rssFeed.setLink(stringBuilder.toString().trim());
                        break;
                    case "description":
                        rssFeed.setDescription(stringBuilder.toString().trim());
                        break;
                    case "language":
                        rssFeed.setLanguage(stringBuilder.toString().trim());
                        break;
                    default:
                        Log.d("RssHandler", "Unhandled feed tag: " + qName);
                }
            } else if (rssItem != null) { // Parse item-level properties
                switch (qName) {
                    case "title":
                        rssItem.setTitle(HtmlEscape.unescapeHtml(stringBuilder.toString().trim()));
                        break;
                    case "link":
                        rssItem.setLink(stringBuilder.toString().trim());
                        break;
                    case "description":
                        rssItem.setDescription(stringBuilder.toString().trim());
                        break;
                    case "media:thumbnail":
                    case "media:content":
                    case "image":
                        rssItem.setImageUrl(imageUrl != null ? imageUrl.trim() : null);
                        imageUrl = null; // Reset after use
                        break;
                    case "pubDate":
                        rssItem.setPubDate(stringBuilder.toString().trim());
                        break;
                    case "category":
                        rssItem.setCategory(stringBuilder.toString().trim());
                        break;
                    default:
                        Log.d("RssHandler", "Unhandled item tag: " + qName);
                }
            } else {
                Log.w("RssHandler", "Unhandled element: " + qName + " with no context.");
            }
        } catch (Exception e) {
            Log.e("RssHandler", "Error processing end element " + qName + ": " + e.getMessage(), e);
        }
    }

    //Goes through character by character when parsing whats inside of a tag.
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        try {
            stringBuilder.append(ch, start, length);
        } catch (Exception e) {
            Log.e("RssHandler", "Error while parsing characters: " + e.getMessage());
        }
    }
}
