package com.umc.todayter.domain.record.repository;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.place.enums.ElementType;
import com.umc.todayter.domain.place.enums.RegionCode;
import com.umc.todayter.domain.place.enums.ThemeType;
import com.umc.todayter.domain.place.repository.PlaceRepository;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.enums.RecordType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class VisitRecordRepositoryTest {

    @Autowired
    private VisitRecordRepository visitRecordRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Test
    void findLatestPerPlaceByMemberId_excludesReviewType_returnsOnlyLatestRecord() {
        Member member = memberRepository.save(Member.create("nickname"));
        Place place = placeRepository.save(place("경복궁"));

        VisitRecord oldRecord = VisitRecord.create(member, place, RecordType.RECORD, 4, "다녀왔어요");
        ReflectionTestUtils.setField(oldRecord, "createdAt", LocalDateTime.of(2026, 6, 1, 10, 0));
        visitRecordRepository.save(oldRecord);

        VisitRecord latestRecord = VisitRecord.create(member, place, RecordType.RECORD, 5, "또 다녀왔어요");
        ReflectionTestUtils.setField(latestRecord, "createdAt", LocalDateTime.of(2026, 6, 5, 10, 0));
        visitRecordRepository.save(latestRecord);

        VisitRecord review = VisitRecord.create(member, place, RecordType.REVIEW, 5, "후기예요");
        ReflectionTestUtils.setField(review, "createdAt", LocalDateTime.of(2026, 6, 10, 10, 0));
        visitRecordRepository.save(review);

        List<VisitRecord> result = visitRecordRepository.findLatestPerPlaceByMemberId(member.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(RecordType.RECORD);
        assertThat(result.get(0).getContent()).isEqualTo("또 다녀왔어요");
    }

    @Test
    void findLatestPerPlaceByMemberId_placeWithOnlyReview_returnsNothing() {
        Member member = memberRepository.save(Member.create("nickname"));
        Place place = placeRepository.save(place("경복궁"));

        visitRecordRepository.save(VisitRecord.create(member, place, RecordType.REVIEW, 5, "후기만 있어요"));

        List<VisitRecord> result = visitRecordRepository.findLatestPerPlaceByMemberId(member.getId());

        assertThat(result).isEmpty();
    }

    private Place place(String name) {
        return Place.builder()
                .name(name)
                .summary("summary")
                .description("description")
                .address("address")
                .regionCode(RegionCode.SEOUL)
                .latitude(37.5665)
                .longitude(126.9780)
                .elementType(ElementType.FIRE)
                .themeType(ThemeType.LOVE)
                .averageRating(0.0)
                .reviewCount(0)
                .editorPick(false)
                .active(true)
                .terrainType("궁궐")
                .loveScore(0)
                .relationshipScore(0)
                .careerScore(0)
                .studyScore(0)
                .restScore(0)
                .transitionScore(0)
                .build();
    }
}
