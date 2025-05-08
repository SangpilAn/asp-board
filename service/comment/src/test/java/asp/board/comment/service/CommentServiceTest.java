package asp.board.comment.service;

import asp.board.comment.entity.Comment;
import asp.board.comment.repository.CommentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @InjectMocks
    CommentService commentService;
    @Mock
    CommentRepository commentRepository;

    @Test
    @DisplayName("삭제할 댓글이 자식이 있으면, 삭제만 표시")
    void deleteShouldMarkDeletedIfHasChildren(){
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Comment comment = createComment(articleId, commentId);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(2L);  // 하위 댓글이 있음

        // when
        commentService.delete(commentId);   // 삭제 서비스 호출

        // then
        verify(comment).delete();   // 해당 댓글은 삭제 마킹처리만 함
    }

    @Test
    @DisplayName("하위 댓글이 없고, 부모 댓글이 삭제되지 않았다면, 타겟 댓글만 삭제")
    void deleteShouldDeleteTargetOnlyIfNotDeletedParent(){
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);     // 삭제 타겟 댓글
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = mock(Comment.class);    // 부모 댓글 모킹 객체
        given(parentComment.getDeleted()).willReturn(false);    // 부모 댓글은 삭제 마킹되지 않음

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);  // 하위 댓글이 없음

        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));  // 부모 댓글이 있음

        // when
        commentService.delete(commentId);   // 삭제 서비스 호출

        // then
        verify(commentRepository).delete(comment);                  // 요청 댓글은 삭제
        verify(commentRepository, never()).delete(parentComment);   // 부모 댓글은 삭제 되지 않는다.
    }

    @Test
    @DisplayName("하위 댓글이 없고, 부모 댓글이 삭제 마킹되었다면, 재귀적으로 모두 삭제")
    void deleteShouldDeleteAllRecursivelyIfDeletedParent(){
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        Comment comment = createComment(articleId, commentId, parentCommentId);     // 삭제 타겟 댓글
        given(comment.isRoot()).willReturn(false);

        Comment parentComment = createComment(articleId, parentCommentId);    // 부모 댓글
        given(parentComment.isRoot()).willReturn(true);         // 부모 댓글은 루트 댓글임
        given(parentComment.getDeleted()).willReturn(true);    // 부모 댓글은 삭제 마킹됨

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.countBy(articleId, commentId, 2L)).willReturn(1L);  // 하위 댓글이 없음

        given(commentRepository.findById(parentCommentId)).willReturn(Optional.of(parentComment));  // 부모 댓글이 있음
        given(commentRepository.countBy(articleId, parentCommentId, 2L)).willReturn(1L);    // 부모 댓글도 타겟 댓글이 삭제되었기 때문에 하위 댓글이 없음

        // when
        commentService.delete(commentId);   // 삭제 서비스 호출

        // then
        verify(commentRepository).delete(comment);          // 요청 댓글 삭제
        verify(commentRepository).delete(parentComment);    // 부모 댓글도 삭제
    }

    private Comment createComment(Long articleId, Long commentId) {
        Comment comment = mock(Comment.class);
        given(comment.getArticleId()).willReturn(articleId);
        given(comment.getCommentId()).willReturn(commentId);
        return comment;
    }

    private Comment createComment(Long articleId, Long commentId, Long parentCommentId) {
        Comment comment = createComment(articleId, commentId);
        given(comment.getParentCommentId()).willReturn(parentCommentId);
        return comment;
    }



}