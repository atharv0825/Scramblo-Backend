package com.scramble.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "article_tags")
@Data
@IdClass(ArticleTagId.class)
public class ArticleTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;
}