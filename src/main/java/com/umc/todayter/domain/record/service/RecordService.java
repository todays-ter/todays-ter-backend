package com.umc.todayter.domain.record.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.record.dto.request.RecordCreateRequest;
import com.umc.todayter.domain.record.dto.response.RecordResponse;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;
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

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordService {

    private static final String IMAGE_KEY_PREFIX = "records";

    private final VisitRecordRepository visitRecordRepository;
    private final VisitRecordImageRepository visitRecordImageRepository;
    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    public List<String> uploadImages(List<MultipartFile> images) {
        return images.stream()
                .map(image -> s3Uploader.upload(image, IMAGE_KEY_PREFIX))
                .toList();
    }

    @Transactional
    public RecordResponse createRecord(RecordCreateRequest request) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        Place place = placeRepository.findById(request.placeId())
                .orElseThrow(() -> new CustomException(RecordErrorCode.PLACE_NOT_FOUND));

        VisitRecord visitRecord = visitRecordRepository.save(
                VisitRecord.create(member, place, request.content(), request.visitedAt())
        );

        List<VisitRecordImage> images = saveImages(visitRecord, request.imageUrls());

        return RecordResponse.from(visitRecord, images);
    }

    private List<VisitRecordImage> saveImages(VisitRecord visitRecord, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }

        List<VisitRecordImage> images = new ArrayList<>();
        for (int sortOrder = 0; sortOrder < imageUrls.size(); sortOrder++) {
            images.add(VisitRecordImage.create(visitRecord, imageUrls.get(sortOrder), sortOrder));
        }

        return visitRecordImageRepository.saveAll(images);
    }
}
