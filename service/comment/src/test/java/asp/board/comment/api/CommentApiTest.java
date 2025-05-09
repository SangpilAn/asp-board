package asp.board.comment.api;

import asp.board.comment.service.response.CommentPageResponse;
import asp.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create(){
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my Comment1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my Comment2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my Comment3", response1.getCommentId(), 1L));

        System.out.printf("commentId = %s%n", response1.getCommentId());
        System.out.printf("\tcommentId = %s%n", response2.getCommentId());
        System.out.printf("\tcommentId = %s%n", response3.getCommentId());

//        commentId = 179226866292719616
//          commentId = 179226866422743040
//          commentId = 179226866460491776
    }

    CommentResponse createComment(CommentCreateRequest request){
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read(){
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 179226866292719616L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete(){
        //        commentId = 179226866292719616
        //          commentId = 179226866422743040
        //          commentId = 179226866460491776
        restClient.delete()
                .uri("/v1/comments/{commentId}", 179226741449261056L)
                .retrieve();
    }

    @Test
    void readAll(){
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Test
    void readAllInfiniteScroll(){
        List<CommentResponse> responses1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {});
        System.out.println("FirstResponse");
        for (CommentResponse comment : responses1) {
            if (!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = responses1.getLast().getParentCommentId();
        Long lastCommentId = responses1.getLast().getCommentId();

        List<CommentResponse> responses2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(lastParentCommentId, lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {});
        System.out.println("SecondResponse");
        for (CommentResponse comment : responses2) {
            if (!comment.getParentCommentId().equals(comment.getCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }


    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
