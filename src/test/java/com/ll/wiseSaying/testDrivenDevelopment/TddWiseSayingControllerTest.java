package com.ll.wiseSaying.testDrivenDevelopment;

import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.io.*;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TddWiseSayingControllerTest {

    private ByteArrayOutputStream outContent;
    private TddWiseSayingController controller;

    @BeforeEach
    void beforeEach() {
        TddAppTest.clear();
        controller = new TddApp().getController();
        outContent = TddTestUtil.setOutToByteArray();
    }

    @AfterEach
    void afterEach() {
        TddTestUtil.clearSetOutToByteArray(outContent);
    }

    @Test
    @DisplayName("빈 테스트")
    void test_Empty() {

    }

    @Test
    @DisplayName("등록")
    void testRegistration() {
        String input = "현재를 사랑하라.\n작자미상";
        Scanner scanner = TddTestUtil.genScanner(input);

        controller.register(scanner);

        String output = outContent.toString();
        assertThat(output)
                .contains("명언 :")
                .contains("작가 :")
                .contains("1번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("등록: 빈 명언")
    void testRegistration_EmptyContent() {
        String input = "\n작자미상";
        Scanner scanner = TddTestUtil.genScanner(input);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> controller.register(scanner));

        String output = outContent.toString();
        assertThat(output)
                .doesNotContain("1번 명언이 등록되었습니다.");
        assertEquals(exception.getMessage(), "명언과 작가를 모두 입력해주세요.");
    }

    @Test
    @DisplayName("단순 검색")
    void testSearchAll() {
        String input = "현재를 사랑하라.\n작자미상";
        Scanner scanner = TddTestUtil.genScanner(input);
        controller.register(scanner);

        controller.search("목록");

        String output = outContent.toString();
        assertThat(output)
                .contains("번호 / 작가 / 명언")
                .contains("1 / 작자미상 / 현재를 사랑하라.")
                .contains("페이지 : [1]");
    }

    @Test
    @DisplayName("페이지 검색")
    void testSearch2ndPage() {
        StringBuilder input = new StringBuilder();
        Scanner scanner;
        for (int i = 1; i <= 10; i++) {
            input.setLength(0);
            input.append("content").append(i).append("\nauthor").append(i).append("\n");
            scanner = TddTestUtil.genScanner(input.toString());
            controller.register(scanner);
        }

        controller.search("목록?page=2");

        String output = outContent.toString();
        assertThat(output)
                .contains("1번 명언이 등록되었습니다.")
                .contains("10번 명언이 등록되었습니다.")
                .contains("번호 / 작가 / 명언")
                .contains("3 / author3 / content3")
                .contains("페이지 : 1 / [2]");
    }

    @Test
    @DisplayName("명언 키워드 검색")
    void testSearchContentKeyword() {
        StringBuilder input = new StringBuilder();
        Scanner scanner;
        for (int i = 0; i < 5; i++) {
            input.setLength(0);
            input.append("content").append(i % 2).append("\nauthor").append(i + 1).append("\n");
            scanner = TddTestUtil.genScanner(input.toString());
            controller.register(scanner);
        }


        controller.search("목록?keywordType=content&keyword=1");

        String output = outContent.toString();
        assertThat(output)
                .contains("번호 / 작가 / 명언")
                .contains("4 / author4 / content1")
                .contains("2 / author2 / content1")
                .doesNotContain("1 / author1 / content0")
                .contains("페이지 :");
    }

    @Test
    @DisplayName("작가 키워드 검색")
    void testSearchAuthorKeyword() {
        StringBuilder input = new StringBuilder();
        Scanner scanner;
        for (int i = 0; i < 5; i++) {
            input.setLength(0);
            input.append("content").append(i + 1).append("\nauthor").append(i % 2).append("\n");
            scanner = TddTestUtil.genScanner(input.toString());
            controller.register(scanner);
        }

        controller.search("목록?keywordType=author&keyword=1");

        String output = outContent.toString();
        assertThat(output)
                .contains("번호 / 작가 / 명언")
                .contains("4 / author1")
                .contains("2 / author1")
                .doesNotContain("1 / author0")
                .contains("페이지 :");
    }

    @Test
    @DisplayName("키워드 복합 검색")
    void testSearchComplexKeyword() {
        StringBuilder input = new StringBuilder();
        Scanner scanner;
        for (int i = 0; i < 18; i++) {
            input.setLength(0);
            input.append("content").append(i + 1).append("\nauthor").append(i % 2).append("\n");
            scanner = TddTestUtil.genScanner(input.toString());
            controller.register(scanner);
        }

        controller.search("목록?page=2&keywordType=author&keyword=1");

        String output = outContent.toString();
        assertThat(output)
                .contains("번호 / 작가 / 명언")
                .contains("2 / author1")
                .contains("페이지 :")
                .contains("[2]");
    }

    @Test
    @DisplayName("잘못된 키워드: 존재하지 않는 항목")
    void testSearch_InvalidSearchKeyword() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () ->controller.search("목록?keyboard=3"));

        String output = outContent.toString();
        assertThat(output)
                .doesNotContain("번호 / 작가 / 명언");
        assertEquals(exception.getMessage(), "명령을 다시 확인해주세요.");
    }

    @Test
    @DisplayName("잘못된 키워드: keywordType&keyword 세트가 아님")
    void testSearch_NotSufficientSearchKeyword() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () ->controller.search("목록?keyword=key"));

        String output = outContent.toString();
        assertThat(output)
                .doesNotContain("번호 / 작가 / 명언");
        assertEquals(exception.getMessage(), "명령을 다시 확인해주세요.");
    }

    @Test
    @DisplayName("검색 결과가 존재하지 않음")
    void testSearch_EmptyResult() {
        controller.search("목록?page=1&keywordType=content&keyword=현재");

        String output = outContent.toString();
        assertThat(output)
                .contains("번호 / 작가 / 명언")
                .contains("검색 결과가 존재하지 않습니다.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("범위를 벗어난 페이지")
    void testSearch_OutOfBounds() {
        controller.search("목록?page=100");

        String output = outContent.toString();
        assertThat(output)
                .contains("번호 / 작가 / 명언")
                .contains("검색 결과가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("수정")
    void testModify() {
        String existInput = "현재를 사랑하라.\n작자미상";
        Scanner scanner = TddTestUtil.genScanner(existInput);
        controller.register(scanner);

        String newInput = "현재와 자신을 사랑하라.\n홍길동";
        scanner = TddTestUtil.genScanner(newInput);

        controller.modify(scanner, "수정?id=1");
        controller.search("목록");

        String output = outContent.toString();
        assertThat(output)
                .contains("명언(기존) : 현재를 사랑하라.")
                .contains("작가(기존) : 작자미상")
                .contains("1 / 홍길동 / 현재와 자신을 사랑하라");
    }

    @Test
    @DisplayName("수정 실패: 명언이 존재하지 않음")
    void testModify_NotFound() {
        String input = "현재와 자신을 사랑하라.\n홍길동";
        Scanner scanner = TddTestUtil.genScanner(input);

        controller.modify(scanner, "수정?id=1");
        controller.search("목록");

        String output = outContent.toString();
        assertThat(output)
                .contains("1번 명언은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("수정 실패: 수정사항이 적절하지 않음")
    void testModify_InvalidArgument() {
        String existInput = "현재를 사랑하라.\n작자미상";
        Scanner existScanner = TddTestUtil.genScanner(existInput);
        controller.register(existScanner);

        String newInput = "\n ";
        Scanner newScanner = TddTestUtil.genScanner(newInput);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> controller.modify(newScanner, "수정?id=1"));

        assertEquals(exception.getMessage(), "정보를 모두 입력해주세요.");
    }

    @Test
    @DisplayName("삭제 성공")
    void testDelete_Success() {
        String input = "현재를 사랑하라.\n작자미상";
        Scanner scanner = TddTestUtil.genScanner(input);
        controller.register(scanner);

        controller.delete("삭제?id=1");

        String output = outContent.toString();
        assertThat(output)
                .contains("1번 명언이 삭제되었습니다.");
    }

    @Test
    @DisplayName("삭제 실패")
    void testDelete_Failure() {
        controller.delete("삭제?id=1");

        String output = outContent.toString();
        assertThat(output)
                .contains("1번 명언은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("빌드")
    void testBuild() throws IOException {
        String input = "현재를 사랑하라.\n작자미상";
        Scanner scanner = TddTestUtil.genScanner(input);
        controller.register(scanner);

        controller.build();

        String output = outContent.toString();
        assertThat(output)
                .contains("data.json 파일의 내용이 갱신되었습니다.");

        File dataFile = new File("db/wiseSaying/data.json");
        assertThat(dataFile.exists()).isTrue();
        String fileContent = Files.readString(dataFile.toPath());
        String expectedContent = """
        [
          {
            "id": 1,
            "content": "현재를 사랑하라.",
            "author": "작자미상"
          }
        ]
        """;
        assertThat(fileContent).isEqualToIgnoringWhitespace(expectedContent);
    }
}
