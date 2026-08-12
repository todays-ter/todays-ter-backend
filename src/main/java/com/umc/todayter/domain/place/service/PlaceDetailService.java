package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.response.PlaceDetailResponse;
import com.umc.todayter.domain.place.dto.response.PlaceReviewItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceReviewListResponse;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.exception.PlaceErrorCode;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.place.repository.SavedPlaceRepository;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;
import com.umc.todayter.domain.record.enums.RecordType;
import com.umc.todayter.domain.record.repository.VisitRecordImageRepository;
import com.umc.todayter.domain.record.repository.VisitRecordRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.SecurityUtil;
import com.umc.todayter.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceDetailService {

    private final PlaceRepository placeRepository;
    private final SavedPlaceRepository savedPlaceRepository;
    private final VisitRecordRepository visitRecordRepository;
    private final VisitRecordImageRepository visitRecordImageRepository;
    private final S3Uploader s3Uploader;

    public PlaceDetailResponse getPlaceDetail(Long placeId, String contextPathUrl) {
        Place place = findActivePlace(placeId);
        Long memberId = SecurityUtil.getCurrentMemberIdOrNull();

        long reviewCount = visitRecordRepository.countByPlaceIdAndType(placeId, RecordType.REVIEW);
        boolean isSaved = memberId != null && savedPlaceRepository.existsByMemberIdAndPlaceId(memberId, placeId);
        boolean isVisited = memberId != null && visitRecordRepository.existsByMemberIdAndPlaceId(memberId, placeId);

        return PlaceDetailResponse.from(place, contextPathUrl, reviewCount, isSaved, isVisited);
    }

    public PlaceReviewListResponse getPlaceReviews(Long placeId) {
        findActivePlace(placeId);
        Long memberId = SecurityUtil.getCurrentMemberIdOrNull();

        List<VisitRecord> reviews = visitRecordRepository.findByPlaceIdAndTypeOrderByCreatedAtDesc(placeId, RecordType.REVIEW);

        List<Long> reviewIds = reviews.stream()
                .map(VisitRecord::getId)
                .toList();
        Map<Long, List<VisitRecordImage>> imagesByRecordId = visitRecordImageRepository
                .findByVisitRecordIdInOrderBySortOrderAsc(reviewIds).stream()
                .collect(Collectors.groupingBy(image -> image.getVisitRecord().getId()));

        PlaceReviewItemResponse myReview = reviews.stream()
                .filter(review -> review.getMember().getId().equals(memberId))
                .findFirst()
                .map(review -> PlaceReviewItemResponse.from(
                        review,
                        imagesByRecordId.getOrDefault(review.getId(), List.of()),
                        s3Uploader::presignedGetUrl
                ))
                .orElse(null);

        List<PlaceReviewItemResponse> otherReviews = reviews.stream()
                .filter(review -> !review.getMember().getId().equals(memberId))
                .map(review -> PlaceReviewItemResponse.from(
                        review,
                        imagesByRecordId.getOrDefault(review.getId(), List.of()),
                        s3Uploader::presignedGetUrl
                ))
                .toList();

        return new PlaceReviewListResponse(reviews.size(), myReview, otherReviews);
    }

    private Place findActivePlace(Long placeId) {
        return placeRepository.findByIdAndActiveTrue(placeId)
                .orElseThrow(() -> new CustomException(PlaceErrorCode.PLACE_NOT_FOUND));
    }
}
