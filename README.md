# Fabric Client-Side 3D Skin Painter

본 프로젝트는 마인크래프트에서 플레이어 모델의 스킨을 실시간으로 편집할 수 있는 **클라이언트 사이드(Fabric) 모드**입니다.

클릭 위치를 레이캐스트로 추적해 스킨 UV 좌표에 직접 색을 칠합니다.

※ 타 플레이어 간 실시간 동기화를 위해서는 전용 플러그인(Paper/Bukkit)이 필수적입니다.

<img width="496" height="515" alt="image" src="https://github.com/user-attachments/assets/7d665d60-01bc-47ba-87b3-0f9defce6240" />

## 조작 방법

| 동작              | 조작                              |
|:----------------|:--------------------------------|
| **페인팅 모드 토글**   | `G`                             |
| **브러쉬 칠하기**     | `좌클릭`                           |
| **영역 채우기**      | `Ctrl + 좌클릭`                    |
| **카메라 회전**      | `우클릭 드래그`                       |
| **스포이드**        | `Alt + 좌클릭`                     |
| **실행 취소 / 재실행** | `Ctrl + Z` / `Ctrl + Shift + Z` |
| **브러시 반경 변경**   | `[`,`]`                         |

## 개발 환경

* **Client-Side:** Minecraft 26.1.1 / Fabric Loader & Fabric API
* **Server-Side:** Paper / Bukkit (26.1.1) 및 [동기화 플러그인](https://github.com/jmsoup/hideandseekserver)
