package com.example.realtimechat.Model;

public class GroupModel {
    private String groupId;
    private String groupTitle;
    private String GroupDesc;
    private String timestamp;
    private String createdBy;
    private String groupIcon;

    public GroupModel() {
    }

    public GroupModel(String groupId, String groupTitle, String groupDesc, String timestamp, String createdBy,String groupIcon) {

        this.groupId = groupId;
        this.groupTitle = groupTitle;
        GroupDesc = groupDesc;
        this.timestamp = timestamp;
        this.createdBy = createdBy;
        this.groupIcon=groupIcon;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupDesc() {
        return GroupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        GroupDesc = groupDesc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
