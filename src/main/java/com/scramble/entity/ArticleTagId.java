package com.scramble.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleTagId implements Serializable {
    private Long article;
    private Long tag;
}