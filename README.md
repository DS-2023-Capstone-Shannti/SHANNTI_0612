# PostureTeacher
### _자세선생 앱_
>경상국립대학교 컴퓨터과학과
>팀장 박주철(19학번), 팀원 류경복(19학번)

# 개요
자세선생(Posture Teacher) 앱은 사람의 앉은 자세 혹은 플랭크 자세를 감지한 다음, 신체 각 지점의 각도와 길이에 따라 올바른 자세 여부를 판별한다. 그리고 판별 결과에 따라 부위마다 자세 상황을 알려주고, 이에 따른 통계를 제공하는 안드로이드 기반의 도우미 애플리케이션이다.

***
# 상세
### 개발 인원
- 팀장 박주철
   - 프로젝트 지휘, 계획, 관리
   - 머신러닝 및 영상 처리 및 각종 최적화
   - 영상 분석 후 올바른 자세에 대한 판별
   - 도움말 및 측정 페이지 제작
- 팀원 류경복
   - 상세 유저 인터페이스 설계
   - 측정 관련 DB 설계
   - 로딩 및 메인 및 통계 페이지 제작

### 개발 기술
본 프로젝트 개발에 사용된 라이브러리 및 파이프라인입니다.
- [Flogger] - Java용 Fluent interface 로깅 API
- [Android Jetpack] - 고품질 개발 라이브러리 및 툴 모음집
- [Mediapipe] - 라이브 머신러닝 파이프라인

### 개발 환경
| 종류 | 목록 |
| ------ | ------ |
| 사용 언어 | Java(1.8), Python(3.7), C++ |
| 개발 도구 | Android Studio(2021.3.1.) - SDK(30.0) & NDK(20.1.5948944), Github |
| OS 환경 | Windows 10, Ubuntu 22.04 LTS |

### 사용 방법
본 프로젝트의 결과물을 시연하는 방법입니다.
- 디버깅 실행
   - 해당 프로젝트를 다운로드 받고, 안드로이드 스튜디오에서 실행시킵니다.
   - 다만 해당 프로젝트는 스마트폰 카메라를 사용하는 관계로 안드로이드 애뮬레이터에서는 작동하지 않습니다.
   - 안드로이드 스튜디오에서 .apk 형식으로 빌드합니다.
   - 애플리케이션을 스마트폰으로 옮긴 다음 실행합니다.
- 플레이스토어 실행
   - [자세선생 플레이스토어]에 접속합니다.
   - 앱을 다운로드 받습니다.
   - 다운로드 완료 후 앱을 실행합니다.

### 포크 & 모듈 & 리포지토리
본 프로젝트의 포크 혹은 별도로 분리되어 개발된 모듈 혹은 추가 리포지토리 목록입니다.
- [Now Repository] - 현 Repository입니다.
- [류경복 Fork] - 류경복이 개발을 위해 만든 Fork입니다.

### 자료
본 개발을 하면서 작성된 보고서 및 발표 자료입니다.
| 보고서 자료 | 발표 자료 |
| ------ | ------ |
| [보고서 PDF 링크](https://drive.google.com/file/d/1_e_FweSufS3RjxWfrffWqWe5y9QB4G0H/view?usp=sharing) | [발표 PPT 링크](https://docs.google.com/presentation/d/1vncCXVw9mJxXfJ1YFctssHMkv47-yOG7/edit?usp=sharing&ouid=106667079864051075882&rtpof=true&sd=true) |

# 기능 추가 및 업데이트
- 앉은 자세 측정 기능 보강 (완)
- 메인 화면에서 측정 기능 전환 (완)
- 플랭크 자세에 따른 측정 기능 추가 (완)
- 앉은 자세 혹은 플랭크 자세에 따른 통계 전환 기능 추가 (완)
- 통계 기능 상세화 (완)
- 여러 버그 수정 (완)


[자세선생 플레이스토어]: <https://play.google.com/store/apps/details?id=com.gnupr.postureteacher>

[Flogger]: <https://github.com/google/flogger>
[Android Jetpack]: <https://github.com/androidx/androidx>
[Mediapipe]: <https://github.com/google/mediapipe>

[Now Repository]: <https://github.com/valur628/Posture-Teacher>
[류경복 Fork]: <https://github.com/anima0314/Posture-Teacher>