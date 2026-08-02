package com.umc.todayter.domain.place.service;

import com.umc.todayter.domain.place.dto.response.PlaceDetailResponse;
import com.umc.todayter.domain.place.dto.response.PlaceReviewItemResponse;
import com.umc.todayter.domain.place.dto.response.PlaceReviewListResponse;
import com.umc.todayter.domain.place.dto.response.PlaceSearchPageResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public PlaceDetailResponse getPlaceDetail(Long placeId, String contextPathUrl) {
        Place place = findActivePlace(placeId);
        Long memberId = SecurityUtil.getCurrentMemberId();

        long reviewCount = visitRecordRepository.countByPlaceIdAndType(placeId, RecordType.REVIEW);
        boolean isSaved = savedPlaceRepository.existsByMemberIdAndPlaceId(memberId, placeId);
        boolean isVisited = visitRecordRepository.existsByMemberIdAndPlaceId(memberId, placeId);

        return PlaceDetailResponse.from(place, contextPathUrl, reviewCount, isSaved, isVisited);
    }

    public PlaceReviewListResponse getPlaceReviews(Long placeId, Pageable pageable) {
        findActivePlace(placeId);

        Page<VisitRecord> reviews = visitRecordRepository.findByPlaceIdAndType(placeId, RecordType.REVIEW, pageable);

        List<Long> reviewIds = reviews.getContent().stream()
                .map(VisitRecord::getId)
                .toList();
        Map<Long, List<VisitRecordImage>> imagesByRecordId = visitRecordImageRepository
                .findByVisitRecordIdInOrderBySortOrderAsc(reviewIds).stream()
                .collect(Collectors.groupingBy(image -> image.getVisitRecord().getId()));

        List<PlaceReviewItemResponse> content = reviews.getContent().stream()
                .map(review -> PlaceReviewItemResponse.from(
                        review,
                        imagesByRecordId.getOrDefault(review.getId(), List.of())
                ))
                .toList();

        return new PlaceReviewListResponse(content, PlaceSearchPageResponse.from(reviews));
    }

    private Place findActivePlace(Long placeId) {
        return placeRepository.findByIdAndActiveTrue(placeId)
                .orElseThrow(() -> new CustomException(PlaceErrorCode.PLACE_NOT_FOUND));
    }
}
