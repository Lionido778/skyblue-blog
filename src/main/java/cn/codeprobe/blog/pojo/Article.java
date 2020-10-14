package cn.codeprobe.blog.pojo;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "sky_article")
public class Article implements Serializable {

    @Id
    private String id;
    @Column(name = "title")
    private String title;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "category_id")
    private String categoryId;
    @Column(name = "content")
    private String content;
    @Column(name = "type")
    // 0表示富文本，1表示markdown
    private String type;
    @Column(name = "state")
    // 0表示删除，1表示已发布，2表示草稿，3表示置顶
    private String state;
    @Column(name = "summary")
    private String summary;
    @Column(name = "cover")
    private String cover;
    //标签需要切割
    @Column(name = "labels")
    private String label;
    @Column(name = "view_count")
    private long viewCount;
    @Column(name = "create_time")
    private Date createTime;
    @Column(name = "update_time")
    private Date updateTime;

    @OneToOne(targetEntity = UserNoPassword.class)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserNoPassword userNoPassword;

    @Transient
    private List<String> labels = new ArrayList<>();

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public UserNoPassword getUserNoPassword() {
        return userNoPassword;
    }

    public void setUserNoPassword(UserNoPassword userNoPassword) {
        this.userNoPassword = userNoPassword;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLabel() {
        this.labels.clear();
        if (this.label != null) {
            if (!this.label.contains("-")) {
                this.labels.add(this.label);
            } else {
                String[] split = label.split("-");
                List<String> labels = Arrays.asList(split);
                this.labels.addAll(labels);
            }
        }
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
