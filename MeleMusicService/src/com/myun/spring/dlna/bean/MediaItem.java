package com.myun.spring.dlna.bean;

import java.util.LinkedList;
import java.util.TreeMap;


public class MediaItem {
    // 閫氱敤灞炴�
    private String itemId = "";
    private String upnp_storageMedium = "";
    private String upnp_writeStatus = "";
    private String dc_title = "";
    private String dc_date = "";
    private String upnp_class = "";
    private LinkedList<TreeMap<String, String>> resList = new LinkedList<TreeMap<String, String>>();
    private LinkedList<TreeMap<String, String>> albumartList = new LinkedList<TreeMap<String, String>>();
    private byte[] image;

    // 瑙嗛锛岄煶涔愬獟浣撳睘鎬�
    private String upnp_album = "";
    private String upnp_artist = "";
    private String dc_creator = "";
    private String upnp_originalTrackNumber = "";
    private String lyricsURI = "";

    public String getUpnp_album() {
        return upnp_album;
    }

    public void setUpnp_album(String upnp_album) {
        this.upnp_album = upnp_album;
    }

    public String getUpnp_artist() {
        return upnp_artist;
    }

    public void setUpnp_artist(String upnp_artist) {
        this.upnp_artist = upnp_artist;
    }

    public String getDc_creator() {
        return dc_creator;
    }

    public void setDc_creator(String dc_creator) {
        this.dc_creator = dc_creator;
    }

    public String getUpnp_originalTrackNumber() {
        return upnp_originalTrackNumber;
    }

    public void setUpnp_originalTrackNumber(String upnp_originalTrackNumber) {
        this.upnp_originalTrackNumber = upnp_originalTrackNumber;
    }

    public String getLyricsURI() {
        return lyricsURI;
    }

    public void setLyricsURI(String lyricsURI) {
        this.lyricsURI = lyricsURI;
    }

    public LinkedList<TreeMap<String, String>> getAlbumartList() {
        return albumartList;
    }

    /**
     * @return the upnp_class
     */
    public String getUpnp_class() {
        return upnp_class;
    }

    /**
     * @param upnp_class
     *            the upnp_class to set
     */
    public void setUpnp_class(String upnp_class) {
        this.upnp_class = upnp_class;
    }

    /**
     * @return the resList
     */
    public LinkedList<TreeMap<String, String>> getResList() {
        return resList;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getUpnp_storageMedium() {
        return upnp_storageMedium;
    }

    public void setUpnp_storageMedium(String upnp_storageMedium) {
        this.upnp_storageMedium = upnp_storageMedium;
    }

    public String getUpnp_writeStatus() {
        return upnp_writeStatus;
    }

    public void setUpnp_writeStatus(String upnp_writeStatus) {
        this.upnp_writeStatus = upnp_writeStatus;
    }

    public String getDc_title() {
        return dc_title;
    }

    public void setDc_title(String dc_title) {
        this.dc_title = dc_title;
    }

    public String getDc_date() {
        return dc_date;
    }

    public void setDc_date(String dc_date) {
        this.dc_date = dc_date;
    }

    public void setResList(LinkedList<TreeMap<String, String>> resList) {
        this.resList = resList;
    }

    public void setAlbumartList(
            LinkedList<TreeMap<String, String>> albumartList) {
        this.albumartList = albumartList;
    }

}
