package asp.board.comment.service;

import asp.board.comment.entity.Comment;
import asp.board.comment.repository.CommentRepository;
import asp.board.comment.service.request.CommentCreateRequest;
import asp.board.comment.service.response.CommentResponse;
import asp.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final Snowflake snowflake = new Snowflake();
    private final CommentRepository commentRepository;

    @Transactional
    public CommentResponse create(CommentCreateRequest request){
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : request.getParentCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if(parentCommentId == null){
            return null;
        }
        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)    // 2deps 이므로 parentComment 는 root 여야함
                .orElseThrow();
    }

    public CommentResponse read(Long commentId){
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId){
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)){
                        comment.delete();   // 자식이 있을 경우 실제 삭제를 하지 않음
                    }else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), 2L) == 2;  // 자기 자신포함 count 가 2개인 경우 자식이 있음
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment);
        if (!comment.isRoot()){
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted)    // 삭제되어 있는지 확인
                    .filter(not(this::hasChildren)) // 자식이 없는 경우
                    .ifPresent(this::delete);   // 삭제 재귀호출
        }
    }

}
