package nextstep.subway.acceptance;

import static nextstep.subway.acceptance.LineSteps.지하철_노선에_지하철_구간_생성_요청;
import static nextstep.subway.acceptance.PathSteps.createSectionCreateParams;
import static nextstep.subway.acceptance.PathSteps.경로_조회_거리_검증;
import static nextstep.subway.acceptance.PathSteps.경로_조회_시간_검증;
import static nextstep.subway.acceptance.PathSteps.경로_조회_요금_검증;
import static nextstep.subway.acceptance.PathSteps.두_역의_최단_거리_경로_조회를_요청;
import static nextstep.subway.acceptance.PathSteps.역의_순서_검증;
import static nextstep.subway.acceptance.PathSteps.지하철_노선_생성_요청;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.utils.AcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

@DisplayName("지하철 경로 검색")
class PathAcceptanceTest extends AcceptanceTest {

    public static final String DURATION = "DURATION";
    public static final String DISTANCE = "DISTANCE";
    public static final String 이상한_타입 = "이상한 타입";
    private Long 교대역;
    private Long 강남역;
    private Long 양재역;
    private Long 남부터미널역;
    private Long 이호선;
    private Long 신분당선;
    private Long 삼호선;

    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        교대역 = 지하철역_생성_요청("교대역").jsonPath().getLong("id");
        강남역 = 지하철역_생성_요청("강남역").jsonPath().getLong("id");
        양재역 = 지하철역_생성_요청("양재역").jsonPath().getLong("id");
        남부터미널역 = 지하철역_생성_요청("남부터미널역").jsonPath().getLong("id");

        이호선 = 지하철_노선_생성_요청("2호선", "green", 교대역, 강남역, 12, 3);
        신분당선 = 지하철_노선_생성_요청("신분당선", "red", 강남역, 양재역, 10, 2);
        삼호선 = 지하철_노선_생성_요청("3호선", "orange", 교대역, 남부터미널역, 2, 2);

        지하철_노선에_지하철_구간_생성_요청(삼호선, createSectionCreateParams(남부터미널역, 양재역, 3, 1));
    }

    @DisplayName("두 역의 최단 거리 경로를 조회한다.")
    @Test
    void findPathByDistance() {
        // when
        ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역, DISTANCE);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        역의_순서_검증(response, 교대역, 남부터미널역, 양재역);
        경로_조회_거리_검증(response, 5L );
    }

    @DisplayName("두 역의 최단 거리 경로를 소요시간으로 조회한다.")
    @Test
    void findPathByTime() {
        // when
        ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역, DURATION);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        역의_순서_검증(response, 교대역, 남부터미널역, 양재역);
        경로_조회_시간_검증(response, 3L );
    }

    // When 출발역에서 도착역까지의 최단 거리 경로 조회를 요청
    // Then 최단 거리 경로를 응답
    // And 총 거리와 소요 시간을 함께 응답함
    // And 지하철 이용 요금도 함께 응답함
    @DisplayName("두 역의 최단 거리 경로의 거리 총합 따라 지하철 요금 응답을 한다.")
    @Test
    void findPathAndCalculateFare() {
        // when
        ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역, DURATION);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        역의_순서_검증(response, 교대역, 남부터미널역, 양재역);
        경로_조회_거리_검증(response, 5L );
        경로_조회_요금_검증(response, 1250);
    }

    // When 출발역에서 도착역까지의 최단 거리 경로 조회를 요청
    // Then 최단 거리 경로를 응답
    // And 총 거리와 소요 시간을 함께 응답함
    // And 지하철 이용 요금도 함께 응답함
    @DisplayName("두 역의 최단 거리 경로의 거리 총합 따라 지하철 요금 응답을 한다.")
    @Test
    void findPathAndCalculateFareMoreThanTenKilometer() {
        // when
        ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 강남역, DISTANCE);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        역의_순서_검증(response, 교대역, 강남역);
        경로_조회_거리_검증(response, 12L );
        경로_조회_요금_검증(response, 1350);
    }

    @DisplayName("오류 케이스: 없는 타입으로 경로를 조회한다.")
    @Test
    void findPathByWrongTypeError() {
        // when
        ExtractableResponse<Response> response = 두_역의_최단_거리_경로_조회를_요청(교대역, 양재역, 이상한_타입);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
