package com.cpt202.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "project_tag")
public class ProjectTag {
    @EmbeddedId
    private ProjectTagId id = new ProjectTagId();

    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id", referencedColumnName = "project_id")
    private Project project;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id", referencedColumnName = "tag_id")
    private Tag tag;

    public ProjectTag() {
    }

    public ProjectTag(Project project, Tag tag) {
        this.project = project;
        this.tag = tag;
    }

    public ProjectTagId getId() {
        return id;
    }

    public void setId(ProjectTagId id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }
}