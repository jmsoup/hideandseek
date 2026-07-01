# Fabric Client-Side 3D Skin Painter

본 프로젝트는 마인크래프트에서 플레이어 모델의 스킨을 실시간으로 편집할 수 있는 **클라이언트 사이드(Fabric) 모드**입니다.

※ 타 플레이어 간 실시간 동기화를 위해서는 전용 서버 사이드 플러그인(Paper/Bukkit) 연동이 필수적입니다.

<img width="496" height="515" alt="image" src="https://github.com/user-attachments/assets/7d665d60-01bc-47ba-87b3-0f9defce6240" />

## 1. 기능

* **캔버스:** 마우스 클릭 위치를 3D 레이캐스팅으로 추적해 스킨 UV 좌표에 직접 색을 칠합니다.
* **브러시:** 선형 보간 처리를 통해 마우스를 빠르게 그어도 선이 끊기지 않고 매끄럽게 이어집니다.
* **스포이드:** 인게임 조명이나 그림자 왜곡의 영향을 받지 않고, 텍스처 원본의 정확한 색상 값을 뽑아냅니다.

## 2. 조작 방법

| 동작              | 조작                              |
|:----------------|:--------------------------------|
| **페인팅 모드 토글**   | `B`                             |
| **브러쉬 칠하기**     | `좌클릭`                           |
| **영역 채우기**      | `Ctrl + 좌클릭`                    |
| **카메라 회전**      | `우클릭 드래그`                       |
| **스포이드**        | `Alt + 좌클릭`                     |
| **실행 취소 / 재실행** | `Ctrl + Z` / `Ctrl + Shift + Z` |
| **브러시 반경 변경**   | `[`,`]`                         |

## 3. 환경 및 빌드

* **Client-Side:** Minecraft 26.1.1 / Fabric Loader & Fabric API
* **Server-Side:** Paper / Bukkit (26.1.1) 및 [전용 플러그인](https://github.com/jmsoup/hideandseekserver)
