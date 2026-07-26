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
import com.umc.todayter.domain.record.dto.response.RecordResponse;
import com.umc.todayter.domain.record.entity.VisitRecord;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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
    void createRecord_savesVisitRecordAndImages() {
        Member member = Member.create("nickname");
        Place place = place();
        RecordCreateRequest request = new RecordCreateRequest(
                1L, "좋았어요", LocalDate.of(2026, 7, 1), List.of("https://img1", "https://img2")
        );

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(visitRecordRepository.save(any(VisitRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(visitRecordImageRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RecordResponse response = recordService.createRecord(request);

        assertThat(response.placeId()).isEqualTo(place.getId());
        assertThat(response.content()).isEqualTo("좋았어요");
        assertThat(response.imageUrls()).containsExactly("https://img1", "https://img2");
    }

    @Test
    void createRecord_throwsWhenPlaceNotFound() {
        Member member = Member.create("nickname");
        RecordCreateRequest request = new RecordCreateRequest(1L, "내용", LocalDate.now(), List.of());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(placeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(RecordErrorCode.PLACE_NOT_FOUND));
    }

    @Test
    void createRecord_throwsWhenMemberNotFound() {
        RecordCreateRequest request = new RecordCreateRequest(1L, "내용", LocalDate.now(), List.of());

        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.createRecord(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void uploadImages_delegatesEachFileToS3Uploader() {
        MultipartFile file1 = new MockMultipartFile("images", "a.jpg", "image/jpeg", new byte[]{1});
        MultipartFile file2 = new MockMultipartFile("images", "b.png", "image/png", new byte[]{2});

        when(s3Uploader.upload(file1, "records")).thenReturn("https://s3/a.jpg");
        when(s3Uploader.upload(file2, "records")).thenReturn("https://s3/b.png");

        List<String> urls = recordService.uploadImages(List.of(file1, file2));

        assertThat(urls).containsExactly("https://s3/a.jpg", "https://s3/b.png");
    }

    private Place place() {
        return Place.create(
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
    }
}
