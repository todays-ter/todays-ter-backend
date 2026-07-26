package com.umc.todayter.domain.record.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.record.dto.request.RecordCreateRequest;
import com.umc.todayter.domain.record.dto.response.ImageInfo;
import com.umc.todayter.domain.record.dto.response.RecordResponse;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;
import com.umc.todayter.domain.record.enums.RecordType;
import com.umc.todayter.domain.record.exception.RecordErrorCode;
import com.umc.todayter.domain.record.repository.VisitRecordImageRepository;
import com.umc.todayter.domain.record.repository.VisitRecordRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.SecurityUtil;
import com.umc.todayter.global.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

    private static final String IMAGE_KEY_PREFIX = "records";
    private static final int MAX_IMAGE_COUNT = 5;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private final VisitRecordRepository visitRecordRepository;
    private final VisitRecordImageRepository visitRecordImageRepository;
    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public List<ImageInfo> uploadImages(List<MultipartFile> images) {
        validateImages(images);

        Member member = getCurrentMember();

        List<VisitRecordImage> saved = new ArrayList<>();
        for (int sortOrder = 0; sortOrder < images.size(); sortOrder++) {
            String imageUrl = s3Uploader.upload(images.get(sortOrder), IMAGE_KEY_PREFIX);
            saved.add(VisitRecordImage.create(member, imageUrl, sortOrder));
        }
        saved = visitRecordImageRepository.saveAll(saved);

        return saved.stream().map(ImageInfo::from).toList();
    }

    @Transactional
    public RecordResponse createRecord(RecordCreateRequest request) {
        Member member = getCurrentMember();

        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() -> new CustomException(RecordErrorCode.PLACE_NOT_FOUND));

        if (request.type() == RecordType.REVIEW
                && visitRecordRepository.existsByMemberIdAndPlaceIdAndType(member.getId(), place.getId(), RecordType.REVIEW)) {
            throw new CustomException(RecordErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // TODO: 방문 인증 기능 구현 후 검증 로직 추가 (#28 관련) — 현재는 스텁으로 항상 통과

        List<VisitRecordImage> images = resolveImages(member.getId(), request.imageIds());

        VisitRecord visitRecord = visitRecordRepository.save(
                VisitRecord.create(member, place, request.type(), request.rating(), request.content())
        );

        images.forEach(image -> image.attachToRecord(visitRecord));

        return RecordResponse.from(visitRecord, images, LocalDateTime.now());
    }

    private Member getCurrentMember() {
        Long memberId = SecurityUtil.getCurrentMemberId();
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private List<VisitRecordImage> resolveImages(Long memberId, List<Long> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return List.of();
        }

        List<VisitRecordImage> images = visitRecordImageRepository.findAllById(imageIds);
        if (images.size() != imageIds.size()) {
            throw new CustomException(RecordErrorCode.IMAGE_NOT_FOUND);
        }

        boolean hasForeignImage = images.stream()
                .anyMatch(image -> !image.getMember().getId().equals(memberId));
        if (hasForeignImage) {
            throw new CustomException(RecordErrorCode.IMAGE_ACCESS_DENIED);
        }

        return images;
    }

    private void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new CustomException(RecordErrorCode.EMPTY_IMAGE_FILE);
        }
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new CustomException(RecordErrorCode.TOO_MANY_IMAGE_FILES);
        }
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                throw new CustomException(RecordErrorCode.EMPTY_IMAGE_FILE);
            }
            if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
                throw new CustomException(RecordErrorCode.IMAGE_FILE_TOO_LARGE);
            }
            String extension = extractExtension(image.getOriginalFilename());
            if (extension == null || !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
                throw new CustomException(RecordErrorCode.INVALID_IMAGE_FILE_TYPE);
            }
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
