package asp.board.article.repository;

import asp.board.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Test
    void findAllTest(){
        List<Article> articleList = articleRepository.findAll(1L, 1499970L, 30L);
        log.info("articles.size : {}", articleList.size());
        for (Article article : articleList) {
            log.info("article : {}", article);
        }
    }

    @Test
    void countTest(){
        long count = articleRepository.count(1L, 10000L);
        log.info("count : {}", count);
    }

}