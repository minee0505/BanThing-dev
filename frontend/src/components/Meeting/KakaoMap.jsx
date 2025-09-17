import React, { useEffect, useRef } from 'react';
import ReactDOMServer from 'react-dom/server';
import { TbMapPinFilled, TbShoppingCartFilled } from "react-icons/tb";
import styles from './KakaoMap.module.scss';

const KakaoMap = ({ onMarkerClick, meetings, selectedMartName, onClearSelectedMart }) => {
    const mapContainer = useRef(null);
    const isMapInitialized = useRef(false);

    useEffect(() => {
        if (isMapInitialized.current || !meetings || meetings.length === 0) return;

        const initializeMap = async () => {
            const meetingsData = meetings;

            try {
                await loadKakaoMapScript();
            } catch (error) {
                console.error(error);
                return;
            }

            window.kakao.maps.load(() => {
                if (mapContainer.current) {
                    const options = {
                        center: new window.kakao.maps.LatLng(37.566826, 126.9786567),
                        level: 9,
                    };
                    const map = new window.kakao.maps.Map(mapContainer.current, options);

                    const uniqueMarts = new Map();
                    meetingsData.forEach(meeting => {
                        if (!uniqueMarts.has(meeting.martId)) {
                            uniqueMarts.set(meeting.martId, meeting);
                        }
                    });

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
                    isMapInitialized.current = true;
                }
            });
        };

        initializeMap();
    }, [meetings, onMarkerClick]);

    return (
        <div className={styles['map-container']}>
            {selectedMartName && (
                <div className={styles.selectedMart}>
                    <TbMapPinFilled />
                    <span>{selectedMartName}</span>
                    {/* 'X' 버튼 추가 */}
                    <button onClick={onClearSelectedMart} className={styles.clearButton}>
                        &times;
                    </button>
                </div>
            )}
            <div ref={mapContainer} className={styles['map-instance']} />
        </div>
    );
};

// ... (loadKakaoMapScript 헬퍼 함수는 이전과 동일)
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