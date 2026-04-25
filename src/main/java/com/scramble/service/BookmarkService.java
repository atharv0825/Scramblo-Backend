package com.scramble.service;

import com.scramble.entity.Article;
import com.scramble.entity.Bookmark;
import com.scramble.entity.User;
import com.scramble.repository.ArticleRepository;
import com.scramble.repository.BookmarkRepository;
import com.scramble.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public boolean toggleBookmark(Long articleId) {

        User user = securityUtils.getCurrentUser();

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        Bookmark existing = bookmarkRepository
                .findByUserIdAndArticleId(user.getId(), articleId)
                .orElse(null);

        if (existing != null) {
            bookmarkRepository.delete(existing);
            return false;
        }

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setArticle(article);

        bookmarkRepository.save(bookmark);

        return true;
    }

    public boolean isBookmarked(Long articleId) {
        User user = securityUtils.getCurrentUser();
        return bookmarkRepository.existsByUserIdAndArticleId(
                user.getId(),
                articleId
        );
    }
}