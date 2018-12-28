package com.myun.spring.dlna.utils;

import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.myun.spring.dlna.bean.MediaItem;

import android.util.Log;




public class DIDL_XMLHandler extends DefaultHandler {
    private final String TAG = "DIDL_XMLHandler";
    private Boolean currentElement = false;
    private String currentValue = null;

    public TreeMap<String, MediaItem> VideoItemList = new TreeMap<String, MediaItem>();
    public TreeMap<String, MediaItem> AudioItemList = new TreeMap<String, MediaItem>();
    public TreeMap<String, MediaItem> ImageItemList = new TreeMap<String, MediaItem>();

    public TreeMap<String, String> streamattributes = null;
    public TreeMap<String, String> albumartattributes = null;

    private MediaItem currentItem = null;

    public DIDL_XMLHandler() {
    }

    /** Called when tag starts */
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        currentValue = "";
        if (qName.equalsIgnoreCase("item")) {
            currentItem = new MediaItem();
            currentItem.setItemId(attributes.getValue("id"));
        } else if (qName.equalsIgnoreCase("upnp:storageMedium")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("upnp:writeStatus")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("dc:title")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("dc:date")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("upnp:class")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("upnp:album")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("upnp:artist")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("dc:creator")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("upnp:originalTrackNumber")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("upnp:lyricsURI")) {
            currentElement = true;
        } else if (qName.equalsIgnoreCase("upnp:albumArtURI")) {
            albumartattributes = new TreeMap<String, String>();

            albumartattributes.put("protocolInfo",
                    attributes.getValue("dlna:profileID"));

            currentElement = true;

        } else if (qName.equalsIgnoreCase("res")) {
            streamattributes = new TreeMap<String, String>();

            for (int attributecount = 0; attributecount < attributes
                    .getLength(); attributecount++) {
                streamattributes.put(attributes.getLocalName(attributecount),
                        attributes.getValue(attributecount));
            }

            currentElement = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (currentElement) {
            currentValue += new String(ch, start, length);
        }
    }

    /** Called when tag closing */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        Log.d(TAG, "endElement qName:" + qName + ";currentValue:"
                + currentValue);
        /** set value */
        if (qName.equalsIgnoreCase("upnp:storageMedium")) {
            currentItem.setUpnp_storageMedium(currentValue);
        } else if (qName.equalsIgnoreCase("upnp:writeStatus")) {
            currentItem.setUpnp_writeStatus(currentValue);
        } else if (qName.equalsIgnoreCase("dc:title")) {
            currentItem.setDc_title(currentValue);
        } else if (qName.equalsIgnoreCase("dc:date")) {
            currentItem.setDc_date(currentValue);
        } else if (qName.equalsIgnoreCase("upnp:class")) {
            currentItem.setUpnp_class(currentValue);
        } else if (qName.equalsIgnoreCase("upnp:album")) {
            currentItem.setUpnp_album(currentValue);
        } else if (qName.equalsIgnoreCase("upnp:artist")) {
            currentItem.setUpnp_artist(currentValue);
        } else if (qName.equalsIgnoreCase("dc:creator")) {
            currentItem.setDc_creator(currentValue);
        } else if (qName.equalsIgnoreCase("upnp:originalTrackNumber")) {
            currentItem.setUpnp_originalTrackNumber(currentValue);
        } else if (qName.equalsIgnoreCase("upnp:lyricsURI")) {
            currentItem.setLyricsURI(currentValue);
        } else if (qName.equalsIgnoreCase("upnp:albumArtURI")) {
            albumartattributes.put("albumArtURI", currentValue);
            currentItem.getAlbumartList().add(albumartattributes);
        } else if (qName.equalsIgnoreCase("res")) {
            streamattributes.put("res", currentValue);
            currentItem.getResList().add(streamattributes);
        } else if (qName.equalsIgnoreCase("item")) {
            if (currentItem.getUpnp_class().equalsIgnoreCase(
                    "object.item.videoItem")
                    || currentItem.getUpnp_class().equalsIgnoreCase(
                            "object.item.videoItem.movie")) {
                Log.d(TAG,
                        "adding object.item.videoItem '"
                                + currentItem.getDc_title() + "' rescount="
                                + currentItem.getResList().size());
                VideoItemList.put(currentItem.getItemId(), currentItem);
            } else if (currentItem.getUpnp_class().equalsIgnoreCase(
                    "object.item.audioItem")
                    || currentItem.getUpnp_class().equalsIgnoreCase(
                            "object.item.audioItem.musicTrack")) {
                Log.d(TAG,
                        "adding object.item.audioItem '"
                                + currentItem.getDc_title() + "' rescount="
                                + currentItem.getResList().size());
                AudioItemList.put(currentItem.getItemId(), currentItem);
            } else if (currentItem.getUpnp_class().equalsIgnoreCase(
                    "object.item.imageItem")
                    || currentItem.getUpnp_class().equalsIgnoreCase(
                            "object.item.imageItem.photo")) {
                Log.d(TAG,
                        "adding object.item.imageItem '"
                                + currentItem.getDc_title() + "' rescount="
                                + currentItem.getResList().size());
                ImageItemList.put(currentItem.getItemId(), currentItem);
            }
        }
        currentElement = false;
        currentValue = null;
    }
}
