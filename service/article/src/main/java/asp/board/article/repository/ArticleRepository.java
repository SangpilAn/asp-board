package asp.board.article.repository;

import asp.board.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(
            value = "select a.article_id, a.title, a.content, a.board_id, a.writer_id, " +
                    "   a.created_at, a.modified_at " +
                    "from (" +
                    "   select article_id " +
                    "   from article " +
                    "   where board_id = :boardId " +
                    "   order by article_id desc " +
                    "   limit :limit offset :offset" +
                    ") t left join article a " +
                    "on t.article_id = a.article_id",
            nativeQuery = true
    )
    List<Article> findAll(
            @Param("boardId") Long boardId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    @Query(
            value = "select count(*) from (" +
                    "   select article_id from article " +
                    "   where board_id = :boardId limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(@Param("boardId") Long boardId, @Param("limit") Long limit);

    @Query(
            value = "select a.article_id, a.title, a.content, a.board_id, a.writer_id, " +
                    "   a.created_at, a.modified_at " +
                    "from article a " +
                    "where board_id = :boardId " +
                    "order by article_id desc limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit
    );

    @Query(
            value = "select a.article_id, a.title, a.content, a.board_id, a.writer_id, " +
                    "   a.created_at, a.modified_at " +
                    "from article a " +
                    "where board_id = :boardId and article_id < :lastArticleId " +
                    "order by article_id desc limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit,
            @Param("lastArticleId") Long lastArticleId
    );
}
