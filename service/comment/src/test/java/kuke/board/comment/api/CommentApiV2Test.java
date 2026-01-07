package kuke.board.comment.api;

import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponse response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponse response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponse response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());

        /**
         * response1.getPath() = 00003
         * response1.getCommentId() = 266979927941189632
         * 	response2.getPath() = 0000300000
         * 	response2.getCommentId() = 266979928226402304
         * 		response3.getPath() = 000030000000000
         * 		response3.getCommentId() = 266979928268345344
         */
    }

    CommentResponse create(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }

    @Test
    void read() {
        CommentResponse response = restClient.get()
                .uri("/v2/comments/{commentId}", 266979927941189632L)
                .retrieve()
                .body(CommentResponse.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v2/comments/{commentId}", 266979927941189632L)
                .retrieve()
                .toBodilessEntity();
    }

    @Test
    void readAll() {
        CommentPageResponse response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=1")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponse comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }

        /**
         * comment.getCommentId() = 266979475191238656
         * comment.getCommentId() = 266979475623251968
         * comment.getCommentId() = 266979475673583616
         * comment.getCommentId() = 266979571882528768
         * comment.getCommentId() = 266979572071272448
         * comment.getCommentId() = 266979572125798400
         * comment.getCommentId() = 266979638555185152
         * comment.getCommentId() = 266979638706180096
         * comment.getCommentId() = 266979638752317440
         * comment.getCommentId() = 266979927941189632
         */
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponse> response1 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        System.out.println("firstPage");
        for (CommentResponse response : response1) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }


        String lastPath = response1.getLast().getPath();
        List<CommentResponse> response2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });

        System.out.println("secondPage");
        for (CommentResponse response : response2) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
