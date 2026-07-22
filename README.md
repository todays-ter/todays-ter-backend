# todays-ter-backend

오늘의 터 BE 레포지토리입니다.

## 📌 Git convention
<details>
<summary><b>작업 규칙</b></summary>

    - 직접 main 브랜치에서 작업 금지, 모든 개발은 develop 브랜치를 기준으로 진행
    - 기능 단위로 브랜치를 생성하여 작업 후 PR을 통해 병합, 브랜치 이름은 기능/작업 내용이 명확하게 드러나도록 작성
    - 모든 작업 시작 전 작업 브랜치에서 최신 develop 브랜치 pull하여 최신 상태 반영 후 작업 시작
    - PR 생성 전 원격 develop 브랜치에 변경 사항이 있는 경우, 작업 브랜치에 develop 브랜치 merge 후 PR 생성
    - PR은 1명 이상의 코드 리뷰 및 승인 후 병합
    
</details>


<details>
<summary><b>브랜치 전략</b></summary>
    
**Github Flow**

```
1. 작업 단위로 Issue 생성
2. develop 브랜치에서 분기한 작업용 브랜치 생성
3. 생성한 브랜치에서 작업 진행
4. 작업 완료 후 develop 브랜치로 Pull Request(PR) 생성
5. 코드 리뷰 및 승인 후 PR merge
6. 배포 가능한 상태 확인 후, CI/CD 파이프라인을 통해 자동 빌드, 테스트, 배포 진행
```

- **main** : 프로젝트가 최종적으로 배포되는 브랜치
    - Production 환경에 언제 배포해도 문제없는 안정(stable) 브랜치입니다.
    - 장애나 긴급 버그 발생 시 main 브랜치에서 핫픽스를 진행합니다.
    - Initial commit을 제외하고 main 브랜치에 직접 커밋하지 않으며, 반드시 Pull Request로만 병합합니다.
- **develop** : 통합 개발 브랜치
    - 새로운 기능 개발 시 main을 기준으로 develop 브랜치를 생성합니다.
    - feature 브랜치들을 병합하는 곳입니다.
    - 모든 기능이 통합되고 버그가 수정된 후 main 브랜치로 PR을 생성합니다.
    - main 브랜치는 항상 안정적이어야 하며, 불완전한 작업은 develop에서 처리합니다.
- **feat/*** : 기능 개발 브랜치
    - 이슈 기반으로 브랜치를 생성하며, 브랜치명은 반드시 `feat/{도메인}-{이슈번호}-{기능명}` 형식을 따릅니다. 예: `feat/auth-3-login`
    - develop 브랜치를 기준으로 분기하여 새로운 기능을 개발합니다.
    - 기능에 대한 버그 수정은 해당 feature 브랜치 내에서 완료 후 develop으로 PR을 생성합니다.

</details>


<details>
<summary><b>Issue/PR</b></summary>
    
**Issue**
```
[Tag] Title
```
- 이슈 내용에는 다음을 포함:
  - 이슈 요약
  - 상세 내용
  - 체크리스트
  - 참고 사항
- Assignees, Labels 지정
  
**Pull Request (PR)**
```
[Tag] Title
```
- PR 내용에는 다음을 포함:
  - 관련 이슈
  - 작업 내용
  - 테스트 결과
  - 스크린샷
  - 참고 사항
- Reviewers, Assignees, Labels 지정

#### Issue/PR Tag 종류
| Tag      | 설명                         |
|----------|-----------------------------|
| **Feat**     | 새로운 기능 추가             |
| **Fix**      | 버그 수정                    |
| **Docs**     | 문서 수정                    |
| **Style**    | 코드 포맷/스타일 변경        |
| **Refactor** | 코드 리팩토링                |
| **Test**     | 테스트 코드 추가/수정        |
| **Chore**    | 빌드/설정/패키지 관련 작업   |
| **Merge**    | 브랜치 병합                  |

</details>


<details>
<summary><b>Commit message</b></summary>

 ```
Type: Title

<body>               

<footer>            
 ```

- 제목 (Subject)
    - 커밋 유형과 간단한 제목 작성
    - 끝부분에 이슈 번호 포함
- 본문 (Body) (선택)
    - 72자 내로 줄바꿈하여 작성
    - 무엇을, 왜 변경했는지 상세히 설명
- 꼬리말 (Footer) (선택)
    - 이슈 트래커와 함께 사용할 때 해결한 이슈나 참고할 부분을 명시
        - ```Resolves: #이슈번호``` → 커밋이 해당 이슈를 해결할 때
        - ```Refs: #이슈번호``` → 커밋이 관련된 참고 이슈일 때
        - ```See also: #이슈번호``` → 관련 이슈를 참조할 때

예:
```
Feat: 오늘의 기운 조회 API 구현 (#12)

사용자의 사주 프로필과 현재 날짜를 기준으로
오늘의 오행 정보를 조회하는 기능을 구현했습니다.

Resolves: #12
```

**Commit message Type 종류**

| Type | 의미 | 사용 예시 |
|------|------|-----------|
| **Feat** | 새로운 기능 추가 | `Feat: 오늘의 운세 조회 API 구현 (#12)` |
| **Fix** | 버그 수정 | `Fix: 장소 상세 조회 null 응답 오류 수정 (#18)` |
| **Docs** | 문서 수정 | `Docs: README Git 컨벤션 수정 (#2)` |
| **Style** | 코드 스타일 변경 (로직 영향 없음) | `Style: import 정리 및 코드 포맷 수정 (#7)` |
| **Refactor** | 코드 리팩토링 (기능 변경 없음) | `Refactor: 추천 점수 계산 로직 분리 (#24)` |
| **Test** | 테스트 코드 추가/수정 | `Test: 장소 추천 서비스 테스트 추가 (#30)` |
| **Chore** | 빌드/설정/기타 작업 | `Chore: Gradle Redis 의존성 추가 (#6)` |
| **Comment** | 주석 추가/수정 | `Comment: 오행 점수 계산 로직 설명 추가 (#25)` |
| **Rename** | 파일/폴더명 변경 | `Rename: FortuneResponseDto 이름 변경 (#14)` |
| **Remove** | 파일/코드 삭제 | `Remove: 사용하지 않는 장소 DTO 삭제 (#21)` |
| **Init** | 프로젝트 초기 세팅 | `Init: Spring Boot 프로젝트 초기화 (#1)` |
| **Merge** | 브랜치 병합 | `Merge: feat/auth-3-kakao-login` |
| **!BREAKING CHANGE** | 하위 호환 불가 변경 | `!BREAKING CHANGE: 장소 추천 API 응답 구조 변경 (#35)` |
| **!HOTFIX** | 운영 긴급 수정 | `!HOTFIX: 로그인 토큰 발급 실패 수정 (#40)` |
    
</details>
