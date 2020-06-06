package com.leverX.blog.service.impl;

import com.leverX.blog.exception.DataBaseException;
import com.leverX.blog.model.Article;
import com.leverX.blog.model.ArticleStatus;
import com.leverX.blog.model.Tag;
import com.leverX.blog.repository.ArticleRepository;
import com.leverX.blog.service.ArticleService;
import com.leverX.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.AccessControlException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired)) //
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final TagService tagService;

    @Override
    public List<Article> findArticleByTag(List<String> tags) {
        tags = tags.stream().map(String::toLowerCase).collect(Collectors.toList());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return articleRepository.findPublicArticlesByTags(tags);
        }
        return articleRepository.findArticlesByTags(tags);
    }

    @Override
    @Transactional
    public Article saveNewArticle(Article newArticle) {
        return articleRepository.save(Article.builder()
                .title(newArticle.getTitle())
                .text(newArticle.getText())
                .articleStatus(newArticle.getArticleStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    @Override
    @Transactional
    public void deleteArticle(Article article) {
        articleRepository.delete(article.getId());
    }

    @Override
    public void changeStatus(Integer id, ArticleStatus articleStatus) {
        articleRepository.getArticleById(id).setArticleStatus(articleStatus);
    }

    @Override
    public List<Article> getPublicArticle() {
        List<Article> publicArticles = new ArrayList<>();
        for (Article article : articleRepository.getAll()) {
            if (article.getArticleStatus().equals(ArticleStatus.PUBLIC)) {
                publicArticles.add(article);
            }
        }
        return publicArticles;
    }

    @Override
    @Transactional
    public Article getArticleForReading(Integer id) throws DataBaseException {
        Article article = articleRepository.getOne(id);
        if (article == null) {
            throw new DataBaseException( "Article with such id " + id + " is not found");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken && article.getArticleStatus() != ArticleStatus.PUBLIC) {
            throw new AccessControlException("You have no permission to view this article. Please, Log in");
        } else {
            Hibernate.initialize(article.getTags());
            return article;
        }
    }
}
