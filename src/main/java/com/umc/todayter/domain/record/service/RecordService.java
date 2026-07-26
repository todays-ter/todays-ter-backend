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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        List<String> uploadedUrls = new ArrayList<>();
        try {
            List<VisitRecordImage> saved = new ArrayList<>();
            for (int sortOrder = 0; sortOrder < images.size(); sortOrder++) {
                String imageUrl = s3Uploader.upload(images.get(sortOrder), IMAGE_KEY_PREFIX);
                uploadedUrls.add(imageUrl);
                saved.add(VisitRecordImage.create(member, imageUrl, sortOrder));
            }
            saved = visitRecordImageRepository.saveAll(saved);

            return saved.stream().map(ImageInfo::from).toList();
        } catch (CustomException e) {
            uploadedUrls.forEach(s3Uploader::delete);
            throw e;
        } catch (Exception e) {
            // DB 저장 실패 등으로 이미 S3에 올라간 파일이 고아로 남지 않도록 정리한다.
            uploadedUrls.forEach(s3Uploader::delete);
            throw new CustomException(RecordErrorCode.IMAGE_UPLOAD_FAILED);
        }
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

        VisitRecord visitRecord;
        try {
            visitRecord = visitRecordRepository.save(
                    VisitRecord.create(member, place, request.type(), request.rating(), request.content())
            );
        } catch (DataIntegrityViolationException e) {
            // existsByMemberIdAndPlaceIdAndType 체크와 저장 사이의 경합으로 두 요청이 동시에
            // 통과했을 때, review_uniqueness_key unique 제약이 여기서 최종적으로 막아준다.
            throw new CustomException(RecordErrorCode.REVIEW_ALREADY_EXISTS);
        }

        images.forEach(image -> image.attachToRecord(visitRecord));

        // 방문 인증 기능이 아직 없어 검증된 방문 시각을 알 수 없으므로 스텁으로 null을 반환한다.
        return RecordResponse.from(visitRecord, images, null);
    }

    public Page<RecordResponse> getMyRecords(RecordType type, Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Page<VisitRecord> visitRecords = (type == null)
                ? visitRecordRepository.findAllByMemberId(memberId, pageable)
                : visitRecordRepository.findAllByMemberIdAndType(memberId, type, pageable);

        List<Long> recordIds = visitRecords.getContent().stream()
                .map(VisitRecord::getId)
                .toList();

        Map<Long, List<VisitRecordImage>> imagesByRecordId = visitRecordImageRepository
                .findByVisitRecordIdInOrderBySortOrderAsc(recordIds)
                .stream()
                .collect(Collectors.groupingBy(image -> image.getVisitRecord().getId()));

        // 방문 인증 기능이 아직 없어 검증된 방문 시각을 알 수 없으므로 스텁으로 null을 반환한다.
        return visitRecords.map(visitRecord -> RecordResponse.from(
                visitRecord,
                imagesByRecordId.getOrDefault(visitRecord.getId(), List.of()),
                null
        ));
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

        boolean hasAlreadyUsedImage = images.stream()
                .anyMatch(image -> image.getVisitRecord() != null);
        if (hasAlreadyUsedImage) {
            throw new CustomException(RecordErrorCode.IMAGE_ALREADY_USED);
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
