import React, { useEffect, useRef } from 'react';
import ReactDOMServer from 'react-dom/server';
import { TbShoppingCartFilled } from "react-icons/tb";
import styles from './KakaoMap.module.scss';

const KakaoMap = ({ onMarkerClick, meetings }) => {
    const mapContainer = useRef(null);
    const isMapInitialized = useRef(false);

    useEffect(() => {
        // ⭐️⭐️⭐️ API를 직접 호출하지 않고, meetings 데이터가 존재하고 지도가 초기화되지 않았을 때만 로직을 실행합니다.
        if (isMapInitialized.current || !meetings || meetings.length === 0) return;

        const initializeMap = async () => {
            console.log("️ 지도 초기화를 시작합니다.");

            // 이제 meetings prop으로 데이터를 받으므로, API를 따로 호출하는 코드는 필요 없습니다.
            const meetingsData = meetings;
            console.log(` 모임 데이터 ${meetingsData.length}건을 성공적으로 가져왔습니다.`);

            try {
                await loadKakaoMapScript();
            } catch (error) {
                console.error(error);
                return;
            }

            window.kakao.maps.load(() => {
                console.log(" Kakao Maps SDK 로드 완료. 지도를 생성합니다.");
                if (mapContainer.current) {
                    const options = {
                        center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
                        level: 9,
                    };
                    const map = new window.kakao.maps.Map(mapContainer.current, options);

                    // 🚨 수정: 마트별로 모임을 그룹화하여 중복 핀을 방지합니다.
                    const uniqueMarts = new Map();
                    meetingsData.forEach(meeting => {
                        if (!uniqueMarts.has(meeting.martId)) {
                            uniqueMarts.set(meeting.martId, meeting);
                        }
                    });

                    // 🚨 수정: 고유한 마트 정보만 순회하며 핀을 찍습니다.
                    uniqueMarts.forEach((meeting) => {
                        const iconString = ReactDOMServer.renderToString(
                            <TbShoppingCartFilled color="#FD79A8" size="32" />
                        );
                        const dataUrl = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(iconString)}`;
                        const imageSize = new window.kakao.maps.Size(32, 32);
                        const markerImage = new window.kakao.maps.MarkerImage(dataUrl, imageSize);

                        const marker = new window.kakao.maps.Marker({
                            position: new window.kakao.maps.LatLng(meeting.latitude, meeting.longitude),
                            title: meeting.martName,
                            image: markerImage,
                        });

                        window.kakao.maps.event.addListener(marker, 'click', () => {
                            onMarkerClick(meeting.martId);
                        });

                        marker.setMap(map);
                    });

                    console.log(" 지도가 성공적으로 생성되었고 커스텀 마커를 표시했습니다.");
                    isMapInitialized.current = true;
                }
            });
        };

        initializeMap();
    }, [meetings, onMarkerClick]); // 🚨 수정: meetings prop이 변경될 때마다 지도를 다시 그립니다.

    return (
        <div className={styles['map-container']}>
            <div ref={mapContainer} className={styles['map-instance']} />
        </div>
    );
};

// 헬퍼 함수
const loadKakaoMapScript = () => {
    return new Promise((resolve, reject) => {
        if (window.kakao && window.kakao.maps) {
            resolve();
            return;
        }
        const kakaoMapKey = import.meta.env.VITE_KAKAO_APP_KEY;
        if (!kakaoMapKey) {
            reject(new Error("Kakao map key is not configured."));
            return;
        }

        const script = document.createElement('script');
        script.id = 'kakao-maps-sdk';
        script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapKey}&autoload=false`;
        script.async = true;
        script.onload = () => resolve();
        script.onerror = () => reject(new Error("Failed to load Kakao map script."));
        document.head.appendChild(script);
    });
};

export default KakaoMap;