package com.umc.todayter.domain.place.client;

import com.umc.todayter.domain.place.config.GooglePlacesProperties;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GooglePlacesClientTest {

    private static final String BASE_URL = "https://places.googleapis.com/v1";
    private static final String API_KEY = "test-google-api-key";

    @Test
    void getFirstPhotoName_requestsPlaceDetailsWithPhotosFieldMask() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(BASE_URL + "/places/test-place-id"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Goog-Api-Key", API_KEY))
                .andExpect(header("X-Goog-FieldMask", "photos"))
                .andRespond(withSuccess("""
                        {"photos":[{"name":"places/test-place-id/photos/photo-resource"}]}
                        """, MediaType.APPLICATION_JSON));

        Optional<String> result = fixture.client.getFirstPhotoName("test-place-id");

        assertThat(result).contains("places/test-place-id/photos/photo-resource");
        fixture.server.verify();
    }

    @Test
    void getFirstPhotoName_encodesGooglePlaceIdAsOnePathSegment() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(BASE_URL + "/places/place-id%2Fwith%2Fslash"))
                .andRespond(withSuccess("""
                        {"photos":[{"name":"places/test-place-id/photos/photo-resource"}]}
                        """, MediaType.APPLICATION_JSON));

        Optional<String> result = fixture.client.getFirstPhotoName("place-id/with/slash");

        assertThat(result).contains("places/test-place-id/photos/photo-resource");
        fixture.server.verify();
    }

    @Test
    void getFirstPhotoName_returnsEmptyWhenPhotosAreNull() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(BASE_URL + "/places/test-place-id"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        Optional<String> result = fixture.client.getFirstPhotoName("test-place-id");

        assertThat(result).isEmpty();
        fixture.server.verify();
    }

    @Test
    void getFirstPhotoName_returnsEmptyWhenPhotosAreEmpty() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(BASE_URL + "/places/test-place-id"))
                .andRespond(withSuccess("""
                        {"photos":[]}
                        """, MediaType.APPLICATION_JSON));

        Optional<String> result = fixture.client.getFirstPhotoName("test-place-id");

        assertThat(result).isEmpty();
        fixture.server.verify();
    }

    @Test
    void getFirstPhotoName_returnsEmptyWhenFirstPhotoNameIsBlank() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(BASE_URL + "/places/test-place-id"))
                .andRespond(withSuccess("""
                        {"photos":[{"name":" "}]}
                        """, MediaType.APPLICATION_JSON));

        Optional<String> result = fixture.client.getFirstPhotoName("test-place-id");

        assertThat(result).isEmpty();
        fixture.server.verify();
    }

    @Test
    void getPhotoUri_requestsPhotoMediaWithoutEncodingPhotoNameSlashes() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(allOf(
                        containsString(BASE_URL + "/places/test-place-id/photos/photo-resource/media"),
                        not(containsString("places%2Ftest-place-id%2Fphotos%2Fphoto-resource%2Fmedia")),
                        containsString("maxWidthPx=800"),
                        containsString("skipHttpRedirect=true"),
                        containsString("key=" + API_KEY)
                )))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"photoUri":"https://lh3.googleusercontent.com/photo"}
                        """, MediaType.APPLICATION_JSON));

        String result = fixture.client.getPhotoUri("places/test-place-id/photos/photo-resource");

        assertThat(result).isEqualTo("https://lh3.googleusercontent.com/photo");
        fixture.server.verify();
    }

    @Test
    void getPhotoUri_throwsInternalServerErrorWhenPhotoUriIsNull() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(containsString("/places/test-place-id/photos/photo-resource/media")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertCommon500(() -> fixture.client.getPhotoUri("places/test-place-id/photos/photo-resource"));
        fixture.server.verify();
    }

    @Test
    void getPhotoUri_throwsInternalServerErrorWhenPhotoUriIsBlank() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(containsString("/places/test-place-id/photos/photo-resource/media")))
                .andRespond(withSuccess("""
                        {"photoUri":" "}
                        """, MediaType.APPLICATION_JSON));

        assertCommon500(() -> fixture.client.getPhotoUri("places/test-place-id/photos/photo-resource"));
        fixture.server.verify();
    }

    @Test
    void getFirstPhotoName_convertsGoogle4xxWithoutExposingBodyOrApiKey() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(BASE_URL + "/places/test-place-id"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"external error body\"}"));

        assertThatThrownBy(() -> fixture.client.getFirstPhotoName("test-place-id"))
                .isInstanceOf(CustomException.class)
                .hasMessageNotContaining(API_KEY)
                .hasMessageNotContaining("external error body")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        fixture.server.verify();
    }

    @Test
    void getPhotoUri_convertsGoogle5xxWithoutExposingBodyOrApiKey() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(containsString("/places/test-place-id/photos/photo-resource/media")))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"external error body\"}"));

        assertThatThrownBy(() -> fixture.client.getPhotoUri("places/test-place-id/photos/photo-resource"))
                .isInstanceOf(CustomException.class)
                .hasMessageNotContaining(API_KEY)
                .hasMessageNotContaining("external error body")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        fixture.server.verify();
    }

    @Test
    void getFirstPhotoName_convertsMalformedJsonWithoutExposingBodyOrApiKey() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(BASE_URL + "/places/test-place-id"))
                .andRespond(withSuccess("{\"photos\":[", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.client.getFirstPhotoName("test-place-id"))
                .isInstanceOf(CustomException.class)
                .hasMessageNotContaining(API_KEY)
                .hasMessageNotContaining("photos")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        fixture.server.verify();
    }

    @Test
    void getPhotoUri_convertsMalformedJsonWithoutExposingBodyOrApiKey() {
        ClientFixture fixture = createFixture(API_KEY);
        fixture.server.expect(once(), requestTo(containsString("/places/test-place-id/photos/photo-resource/media")))
                .andRespond(withSuccess("{\"photoUri\":", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> fixture.client.getPhotoUri("places/test-place-id/photos/photo-resource"))
                .isInstanceOf(CustomException.class)
                .hasMessageNotContaining(API_KEY)
                .hasMessageNotContaining("photoUri")
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        fixture.server.verify();
    }

    @Test
    void getFirstPhotoName_convertsTimeoutToInternalServerError() {
        RestClient restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory((uri, httpMethod) -> {
                    throw new SocketTimeoutException("timeout");
                })
                .build();
        GooglePlacesClient client = new GooglePlacesClient(restClient, properties(API_KEY));

        assertCommon500(() -> client.getFirstPhotoName("test-place-id"));
    }

    @Test
    void getFirstPhotoName_doesNotSendRequestWhenApiKeyIsBlank() {
        ClientFixture fixture = createFixture(" ");

        assertCommon500(() -> fixture.client.getFirstPhotoName("test-place-id"));
        fixture.server.verify();
    }

    @Test
    void getPhotoUri_doesNotSendRequestWhenApiKeyIsBlank() {
        ClientFixture fixture = createFixture(" ");

        assertCommon500(() -> fixture.client.getPhotoUri("places/test-place-id/photos/photo-resource"));
        fixture.server.verify();
    }

    private void assertCommon500(ThrowingCallable callable) {
        assertThatThrownBy(callable::call)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ClientFixture createFixture(String apiKey) {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder
                .baseUrl(BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        return new ClientFixture(new GooglePlacesClient(restClient, properties(apiKey)), server);
    }

    private GooglePlacesProperties properties(String apiKey) {
        return new GooglePlacesProperties(
                BASE_URL,
                apiKey,
                800,
                Duration.ofSeconds(3),
                Duration.ofSeconds(5)
        );
    }

    private record ClientFixture(
            GooglePlacesClient client,
            MockRestServiceServer server
    ) {
    }

    @FunctionalInterface
    private interface ThrowingCallable {
        void call();
    }
}
