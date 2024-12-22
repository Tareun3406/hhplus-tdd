# 항해플러스 [1주차 과제] TDD로 개발하기
## 사전 준비
- 총 4가지 기본 기능 (포인트 조회, 포인트 충전/사용 내역 조회, 충전, 사용) 을 구현합니다.
- /point 패키지 (디렉토리) 내에 PointService 기본 기능 작성
- /database 패키지의 구현체는 수정하지 않고, 이를 활용해 기능을 구현
- 각 기능에 대한 단위 테스트 작성

## 요구사항
- PATCH  `/point/{id}/charge` : 포인트를 충전한다.
- PATCH `/point/{id}/use` : 포인트를 사용한다.
- *GET `/point/{id}` : 포인트를 조회한다.*
- *GET `/point/{id}/histories` : 포인트 내역을 조회한다.*
- *잔고가 부족할 경우, 포인트 사용은 실패하여야 합니다.*
- *동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.*

## Step 1 기본과제
- 포인트 충전, 사용에 대한 정책 추가 (잔고 부족, 최대 잔고 등)
- 동시에 여러 요청이 들어오더라도 순서대로 (혹은 한번에 하나의 요청씩만) 제어될 수 있도록 리팩토링
- 동시성 제어에 대한 통합 테스트 작성

## Step 2 심화 과제
- 동시성 제어 방식에 대한 분석 및 보고서 작성 ( README.md )
--- 
### 동시성 제어가 필요한 이유
사용자 포인트 시스템을 예로 동시에 포인트를 사용하거나 충전할 경우 </br>
사용자의 포인트가 잘못 계산되거나 예상치 못한 값으로 저장될 수 있습니다. </br>
이러한 상황을 방지하기 위해 동시 처리시 충돌을 방지하여 데이터를 일관성 있고 안전하게 처리하는 것이 중요합니다.

### 동시성 제어에 사용할 수 있는 방법
#### synchronized/@Syncronized
메서드 자체를 잠금으로써 둘 이상의 요청이 있을경우 항상 하나씩 실행합니다. <br/>
실제 충돌 가능성과 관계없이 무조건 하나씩 실행하게 되므로 <b>큰 성능 저하</b>가 있습니다. <br/>
동시성 제어에서 가장 쉬운 방법이지만 여기서는 사용하지 않았습니다. <br/><br/>

#### ReentrantLock
명시적으로 락을 잠그고 해제하여 보다 유연하게 동시성을 제어할 수 있습니다. <br/>
공정성(fair) 모드를 설정하여 요청 순서대로 자원을 획득할 수 있도록 할 수 있습니다. <br/>
여기서는 ConcurrnetHashMap 과 같이 사용하여 유저Id 단위로 고유한 락을 사용하였습니다. <br/>
> 공정 모드와 비공정 모드 <br>
> 공정(fair) 모드: ReentrantLock(true) 로 활성화 합니다. 락 해제시 먼저 요청한 순서대로 자원을 획득합니다. <br>
> 비공정(non-fair/unfair) 모드: 락이 해제될경우 요청 순서와 관계없이 경쟁적으로 자원을 획득합니다. 평균 대기시간이 짧아질 수 있지만 특정 쓰레드가 불리해질 가능성이 있습니다.
<br/><br/>

#### ConcurrentHashMap
여러 스레드가 동시에 접근하더라도 안전하게 사용할 수 있습니다. <br/>
내부적으로 특정 키에 대해 락을 걸어 성능을 최적화합니다.
여기서는 ConcurrentHashMap에 id를 키로하여 ReentrantLock객체를 저장함으로서, id 마다 유니크한 락을 가질 수 있도록 구현하였습니다.
