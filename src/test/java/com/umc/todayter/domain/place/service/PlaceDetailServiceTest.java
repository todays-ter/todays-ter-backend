package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.place.dto.response.PlaceDetailResponse;
import com.umc.todayter.domain.place.dto.response.PlaceReviewListResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.exception.PlaceErrorCode;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.SavedPlaceRepository;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;
import com.umc.todayter.domain.record.enums.RecordType;
import com.umc.todayter.domain.record.repository.VisitRecordImageRepository;
import com.umc.todayter.domain.record.repository.VisitRecordRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceDetailServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private SavedPlaceRepository savedPlaceRepository;

    @Mock
    private VisitRecordRepository visitRecordRepository;

    @Mock
    private VisitRecordImageRepository visitRecordImageRepository;

    @InjectMocks
    private PlaceDetailService placeDetailService;

    @BeforeEach
    void setUpAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new AuthPrincipal(1L), null, Collections.emptyList())
        );
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPlaceDetail_returnsDetailWithSavedAndVisitedTrue() {
        Place place = place(ElementType.WATER, "https://google-place-id");
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.countByPlaceIdAndType(1L, RecordType.REVIEW)).thenReturn(9L);
        when(savedPlaceRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(true);
        when(visitRecordRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(true);

        PlaceDetailResponse response = placeDetailService.getPlaceDetail(1L, "http://localhost");

        assertThat(response.placeId()).isEqualTo(1L);
        assertThat(response.placeName()).isEqualTo("청계천 모전교");
        assertThat(response.imageUrl()).isEqualTo("http://localhost/places/1/thumbnail");
        assertThat(response.element()).isEqualTo("수");
        assertThat(response.hashtags()).containsExactly("하천");
        assertThat(response.description().question()).isEqualTo("이 터의 특징은 무엇인가요?");
        assertThat(response.description().answer()).contains("수(水)");
        assertThat(response.reviewCount()).isEqualTo(9L);
        assertThat(response.isSaved()).isTrue();
        assertThat(response.isVisited()).isTrue();
    }

    @Test
    void getPlaceDetail_returnsNullImageUrlWhenNoGooglePlaceId() {
        Place place = place(ElementType.FIRE, null);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.countByPlaceIdAndType(1L, RecordType.REVIEW)).thenReturn(0L);
        when(savedPlaceRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(false);
        when(visitRecordRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(false);

        PlaceDetailResponse response = placeDetailService.getPlaceDetail(1L, "http://localhost");

        assertThat(response.imageUrl()).isNull();
        assertThat(response.isSaved()).isFalse();
        assertThat(response.isVisited()).isFalse();
    }

    @Test
    void getPlaceDetail_placeNotFound_throwsPlaceNotFound() {
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeDetailService.getPlaceDetail(1L, "http://localhost"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.PLACE_NOT_FOUND);
    }

    @ParameterizedTest
    @EnumSource(ElementType.class)
    void getPlaceDetail_everyElementTypeHasADescription(ElementType elementType) {
        Place place = place(elementType, null);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.countByPlaceIdAndType(1L, RecordType.REVIEW)).thenReturn(0L);
        when(savedPlaceRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(false);
        when(visitRecordRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(false);

        PlaceDetailResponse response = placeDetailService.getPlaceDetail(1L, "http://localhost");

        assertThat(response.description().answer()).isNotBlank();
    }

    @Test
    void getPlaceReviews_returnsAllReviewsWithImagesGroupedByRecord_noMyReview() {
        Place place = place(ElementType.WATER, null);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));

        Member otherMember = member(2L, "리뷰어");
        VisitRecord review = visitRecord(10L, otherMember, place);
        when(visitRecordRepository.findByPlaceIdAndTypeOrderByCreatedAtDesc(1L, RecordType.REVIEW))
                .thenReturn(List.of(review));

        VisitRecordImage image = VisitRecordImage.create(otherMember, "https://cdn/1.jpg", 0);
        ReflectionTestUtils.setField(image, "visitRecord", review);
        when(visitRecordImageRepository.findByVisitRecordIdInOrderBySortOrderAsc(anyList()))
                .thenReturn(List.of(image));

        PlaceReviewListResponse response = placeDetailService.getPlaceReviews(1L);

        assertThat(response.totalCount()).isEqualTo(1);
        assertThat(response.myReview()).isNull();
        assertThat(response.reviews()).hasSize(1);
        assertThat(response.reviews().get(0).reviewId()).isEqualTo(10L);
        assertThat(response.reviews().get(0).writerNickname()).isEqualTo("리뷰어");
        assertThat(response.reviews().get(0).images()).extracting("imageUrl").containsExactly("https://cdn/1.jpg");
    }

    @Test
    void getPlaceReviews_onlyQueriesReviewType_notRecordType() {
        Place place = place(ElementType.WATER, null);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.findByPlaceIdAndTypeOrderByCreatedAtDesc(1L, RecordType.REVIEW))
                .thenReturn(List.of());
        when(visitRecordImageRepository.findByVisitRecordIdInOrderBySortOrderAsc(anyList()))
                .thenReturn(List.of());

        placeDetailService.getPlaceReviews(1L);

        // RECORD 타입("나에게 맞는 터" 개인 기록)은 절대 조회하지 않는지 검증
        verify(visitRecordRepository).findByPlaceIdAndTypeOrderByCreatedAtDesc(1L, RecordType.REVIEW);
        verify(visitRecordRepository, org.mockito.Mockito.never())
                .findByPlaceIdAndTypeOrderByCreatedAtDesc(1L, RecordType.RECORD);
    }

    @Test
    void getPlaceDetail_reviewCount_onlyCountsReviewType_notRecordType() {
        Place place = place(ElementType.WATER, null);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.countByPlaceIdAndType(1L, RecordType.REVIEW)).thenReturn(3L);
        when(savedPlaceRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(false);
        when(visitRecordRepository.existsByMemberIdAndPlaceId(1L, 1L)).thenReturn(false);

        PlaceDetailResponse response = placeDetailService.getPlaceDetail(1L, "http://localhost");

        assertThat(response.reviewCount()).isEqualTo(3L);
        verify(visitRecordRepository).countByPlaceIdAndType(1L, RecordType.REVIEW);
        verify(visitRecordRepository, org.mockito.Mockito.never())
                .countByPlaceIdAndType(1L, RecordType.RECORD);
    }

    @Test
    void getPlaceReviews_separatesMyReviewFromOtherReviews() {
        Place place = place(ElementType.WATER, null);
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(place));

        Member me = member(1L, "나"); // 현재 인증된 회원(1L)과 동일
        Member otherMember = member(2L, "리뷰어");
        VisitRecord myReview = visitRecord(11L, me, place);
        VisitRecord otherReview = visitRecord(10L, otherMember, place);
        when(visitRecordRepository.findByPlaceIdAndTypeOrderByCreatedAtDesc(1L, RecordType.REVIEW))
                .thenReturn(List.of(myReview, otherReview));
        when(visitRecordImageRepository.findByVisitRecordIdInOrderBySortOrderAsc(anyList()))
                .thenReturn(List.of());

        PlaceReviewListResponse response = placeDetailService.getPlaceReviews(1L);

        assertThat(response.totalCount()).isEqualTo(2);
        assertThat(response.myReview()).isNotNull();
        assertThat(response.myReview().reviewId()).isEqualTo(11L);
        assertThat(response.reviews()).hasSize(1);
        assertThat(response.reviews().get(0).reviewId()).isEqualTo(10L);
    }

    @Test
    void getPlaceReviews_placeNotFound_throwsPlaceNotFound() {
        when(placeRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> placeDetailService.getPlaceReviews(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(PlaceErrorCode.PLACE_NOT_FOUND);
    }

    private Member member(Long id, String nickname) {
        Member member = Member.create(nickname);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private VisitRecord visitRecord(Long id, Member member, Place place) {
        VisitRecord visitRecord = VisitRecord.create(member, place, RecordType.REVIEW, 4, "좋았어요");
        ReflectionTestUtils.setField(visitRecord, "id", id);
        return visitRecord;
    }

    private Place place(ElementType elementType, String googlePlaceId) {
        Place place = Place.builder()
                .name("청계천 모전교")
                .summary("summary")
                .description("description")
                .address("서울 중구 무교동")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5665)
                .longitude(126.9780)
                .elementType(elementType)
                .themeType(ThemeType.HEALTH)
                .averageRating(0.0)
                .reviewCount(0)
                .editorPick(false)
                .active(true)
                .terrainType("하천")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(0)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
        ReflectionTestUtils.setField(place, "id", 1L);
        ReflectionTestUtils.setField(place, "googlePlaceId", googlePlaceId);
        return place;
    }
}
