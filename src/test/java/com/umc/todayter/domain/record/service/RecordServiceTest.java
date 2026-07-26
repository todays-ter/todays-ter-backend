package com.umc.todayter.domain.record.service;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
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
import com.umc.todayter.global.security.AuthPrincipal;
import com.umc.todayter.global.util.S3Uploader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private VisitRecordRepository visitRecordRepository;

    @Mock
    private VisitRecordImageRepository visitRecordImageRepository;

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Uploader s3Uploader;

    @InjectMocks
    private RecordService recordService;

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
    void createRecord_savesVisitRecordWithoutImages() {
        Member member = member(1L);
        Place place = place();
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.RECORD, 4, "좋았어요", null);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.save(any(VisitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordResponse response = recordService.createRecord(request);

        assertThat(response.placeId()).isEqualTo(place.getId());
        assertThat(response.rating()).isEqualTo(4);
        assertThat(response.content()).isEqualTo("좋았어요");
        assertThat(response.images()).isEmpty();
        assertThat(response.visitVerifiedAt()).isNull();
    }

    @Test
    void createRecord_review_attachesOwnedImages() {
        Member member = member(1L);
        Place place = place();
        VisitRecordImage image = image(101L, member, "https://s3/a.jpg");
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.REVIEW, 5, "후기입니다", List.of(101L));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.existsByMemberIdAndPlaceIdAndType(1L, place.getId(), RecordType.REVIEW)).thenReturn(false);
        when(visitRecordImageRepository.findAllById(List.of(101L))).thenReturn(List.of(image));
        when(visitRecordRepository.save(any(VisitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordResponse response = recordService.createRecord(request);

        assertThat(response.images()).extracting(ImageInfo::imageId).containsExactly(101L);
    }

    @Test
    void createRecord_throwsWhenReviewAlreadyExists() {
        Member member = member(1L);
        Place place = place();
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.REVIEW, 5, "후기입니다", null);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.existsByMemberIdAndPlaceIdAndType(1L, place.getId(), RecordType.REVIEW)).thenReturn(true);

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.REVIEW_ALREADY_EXISTS));
    }

    @Test
    void createRecord_throwsWhenPlaceNotFound() {
        Member member = member(1L);
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.RECORD, 4, "내용", null);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.PLACE_NOT_FOUND));
    }

    @Test
    void createRecord_throwsWhenMemberNotFound() {
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.RECORD, 4, "내용", null);

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void createRecord_throwsWhenImageNotFound() {
        Member member = member(1L);
        Place place = place();
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.RECORD, 4, "내용", List.of(101L, 102L));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordImageRepository.findAllById(List.of(101L, 102L))).thenReturn(List.of(image(101L, member, "https://s3/a.jpg")));

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.IMAGE_NOT_FOUND));
    }

    @Test
    void createRecord_throwsWhenImageOwnedByAnotherMember() {
        Member member = member(1L);
        Member otherMember = member(2L);
        Place place = place();
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.RECORD, 4, "내용", List.of(101L));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordImageRepository.findAllById(List.of(101L))).thenReturn(List.of(image(101L, otherMember, "https://s3/a.jpg")));

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.IMAGE_ACCESS_DENIED));
    }

    @Test
    void createRecord_throwsWhenImageAlreadyUsed() {
        Member member = member(1L);
        Place place = place();
        VisitRecordImage image = image(101L, member, "https://s3/a.jpg");
        VisitRecord existingRecord = VisitRecord.create(member, place, RecordType.RECORD, 3, "이전 기록");
        image.attachToRecord(existingRecord);
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.RECORD, 4, "내용", List.of(101L));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordImageRepository.findAllById(List.of(101L))).thenReturn(List.of(image));

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.IMAGE_ALREADY_USED));
    }

    @Test
    void createRecord_convertsSaveRaceConditionToReviewAlreadyExists() {
        Member member = member(1L);
        Place place = place();
        RecordCreateRequest request = new RecordCreateRequest(1L, RecordType.REVIEW, 5, "후기입니다", null);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.existsByMemberIdAndPlaceIdAndType(1L, place.getId(), RecordType.REVIEW)).thenReturn(false);
        when(visitRecordRepository.save(any(VisitRecord.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.REVIEW_ALREADY_EXISTS));
    }

    @Test
    void uploadImages_delegatesEachFileToS3UploaderAndReturnsImageIds() {
        Member member = member(1L);
        MultipartFile file1 = new MockMultipartFile("images", "a.jpg", "image/jpeg", new byte[]{1});
        MultipartFile file2 = new MockMultipartFile("images", "b.png", "image/png", new byte[]{2});

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(s3Uploader.upload(file1, "records")).thenReturn("https://s3/a.jpg");
        when(s3Uploader.upload(file2, "records")).thenReturn("https://s3/b.png");
        when(visitRecordImageRepository.saveAll(any())).thenAnswer(invocation -> {
            List<VisitRecordImage> images = invocation.getArgument(0);
            for (int i = 0; i < images.size(); i++) {
                ReflectionTestUtils.setField(images.get(i), "id", 100L + i);
            }
            return images;
        });

        List<ImageInfo> result = recordService.uploadImages(List.of(file1, file2));

        assertThat(result).extracting(ImageInfo::imageUrl)
                .containsExactly("https://s3/a.jpg", "https://s3/b.png");
    }

    @Test
    void uploadImages_throwsWhenEmpty() {
        assertThatThrownBy(() -> recordService.uploadImages(List.of()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.EMPTY_IMAGE_FILE));
    }

    @Test
    void uploadImages_throwsWhenTooManyFiles() {
        List<MultipartFile> files = List.of(
                jpg("a.jpg"), jpg("b.jpg"), jpg("c.jpg"), jpg("d.jpg"), jpg("e.jpg"), jpg("f.jpg")
        );

        assertThatThrownBy(() -> recordService.uploadImages(files))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.TOO_MANY_IMAGE_FILES));
    }

    @Test
    void uploadImages_throwsWhenInvalidExtension() {
        MultipartFile file = new MockMultipartFile("images", "a.gif", "image/gif", new byte[]{1});

        assertThatThrownBy(() -> recordService.uploadImages(List.of(file)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.INVALID_IMAGE_FILE_TYPE));
    }

    @Test
    void uploadImages_throwsWhenFileTooLarge() {
        byte[] oversized = new byte[11 * 1024 * 1024];
        MultipartFile file = new MockMultipartFile("images", "a.jpg", "image/jpeg", oversized);

        assertThatThrownBy(() -> recordService.uploadImages(List.of(file)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.IMAGE_FILE_TOO_LARGE));
    }

    @Test
    void uploadImages_succeedsWithFiveFilesJustUnderPerFileLimit() {
        Member member = member(1L);
        byte[] justUnderLimit = new byte[10 * 1024 * 1024 - 1];
        List<MultipartFile> files = List.of(
                new MockMultipartFile("images", "a.jpg", "image/jpeg", justUnderLimit),
                new MockMultipartFile("images", "b.jpg", "image/jpeg", justUnderLimit),
                new MockMultipartFile("images", "c.jpg", "image/jpeg", justUnderLimit),
                new MockMultipartFile("images", "d.jpg", "image/jpeg", justUnderLimit),
                new MockMultipartFile("images", "e.jpg", "image/jpeg", justUnderLimit)
        );

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(s3Uploader.upload(any(), org.mockito.ArgumentMatchers.eq("records"))).thenReturn("https://s3/x.jpg");
        when(visitRecordImageRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ImageInfo> result = recordService.uploadImages(files);

        assertThat(result).hasSize(5);
    }

    @Test
    void uploadImages_deletesUploadedFilesFromS3WhenSaveFails() {
        Member member = member(1L);
        MultipartFile file1 = new MockMultipartFile("images", "a.jpg", "image/jpeg", new byte[]{1});
        MultipartFile file2 = new MockMultipartFile("images", "b.jpg", "image/jpeg", new byte[]{2});

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(s3Uploader.upload(file1, "records")).thenReturn("https://s3/a.jpg");
        when(s3Uploader.upload(file2, "records")).thenReturn("https://s3/b.jpg");
        when(visitRecordImageRepository.saveAll(any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("boom"));

        assertThatThrownBy(() -> recordService.uploadImages(List.of(file1, file2)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.IMAGE_UPLOAD_FAILED));

        org.mockito.Mockito.verify(s3Uploader).delete("https://s3/a.jpg");
        org.mockito.Mockito.verify(s3Uploader).delete("https://s3/b.jpg");
    }

    private MultipartFile jpg(String filename) {
        return new MockMultipartFile("images", filename, "image/jpeg", new byte[]{1});
    }

    private Member member(Long id) {
        Member member = Member.create("nickname" + id);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private VisitRecordImage image(Long id, Member owner, String imageUrl) {
        VisitRecordImage image = VisitRecordImage.create(owner, imageUrl, 0);
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }

    private Place place() {
        Place place = Place.create(
                "북촌한옥마을",
                "한옥/골목",
                "설명",
                "서울특별시 종로구",
                RegionCode.SEOUL,
                37.5826,
                126.983,
                ElementType.EARTH,
                ThemeType.RELATIONSHIP,
                0.0,
                0,
                false,
                true
        );
        ReflectionTestUtils.setField(place, "id", 1L);
        return place;
    }
}
