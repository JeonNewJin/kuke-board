package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiTest {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = createComment(new CommentCreateRequest(1L, "my comment 1", null, 1L));
        CommentResponse response2 = createComment(new CommentCreateRequest(1L, "my comment 2", response1.getCommentId(), 1L));
        CommentResponse response3 = createComment(new CommentCreateRequest(1L, "my comment 3", response1.getCommentId(), 1L));

        System.out.println("commentId=%s".formatted(response1.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response2.getCommentId()));
        System.out.println("\tcommentId=%s".formatted(response3.getCommentId()));
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v1/comments/{commentId}", 265072445573881856L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response);
    }

    @Test
    void delete() {
//        commentId=265072445573881856
//          commentId=265072445993312256
//          commentId=265072446047838208

        restClient.delete()
                .uri("/v1/comments/{commentId}", 265072446047838208L)
                .retrieve()
                .toBodilessEntity();
        ;
    }

    CommentResponse createComment(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v1/comments?articleId=1&pageSize=10&page=1")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /**
         * 1번 페이지 수행 결과
         * comment.getCommentId() = 265074051223064576
         * 	comment.getCommentId() = 265074051248230405
         * comment.getCommentId() = 265074051223064577
         * 	comment.getCommentId() = 265074051248230408
         * comment.getCommentId() = 265074051223064578
         * 	comment.getCommentId() = 265074051248230402
         * comment.getCommentId() = 265074051223064579
         * 	comment.getCommentId() = 265074051248230409
         * comment.getCommentId() = 265074051223064580
         * 	comment.getCommentId() = 265074051248230406
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("firstPage");
        for (CommentResponse comment : response1) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        Long lastParentCommentId = response1.getLast().getParentCommentId();
        Long lastCommentId = response1.getLast().getCommentId();

        List<CommentResponse> response2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5&lastParentCommentId=%s&lastCommentId=%s"
                        .formatted(lastParentCommentId, lastCommentId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("secondPage");
        for (CommentResponse comment : response2) {
            if (!comment.getCommentId().equals(comment.getParentCommentId())) {
                System.out.print("\t");
            }
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long parentCommentId;
        private Long writerId;
    }
}
