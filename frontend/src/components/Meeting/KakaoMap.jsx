import React, { useEffect, useRef } from 'react';
import ReactDOMServer from 'react-dom/server';
import { TbShoppingCartFilled } from "react-icons/tb";
import { getAllMarts } from '../../services/MartApi.js';
import styles from './KakaoMap.module.scss';

const KakaoMap = () => {
    const mapContainer = useRef(null);
    const isMapInitialized = useRef(false);

    useEffect(() => {
        if (isMapInitialized.current) return;

        const initializeMap = async () => {
            console.log("️ 지도 초기화를 시작합니다.");

            const result = await getAllMarts();
            if (!result.success || result.data.length === 0) {
                console.error(" 마트 데이터를 가져오지 못했거나 데이터가 없습니다.");
                return;
            }
            const martsData = result.data;
            console.log(` 마트 데이터 ${result.data.length}건을 성공적으로 가져왔습니다.`);

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

                    // 마트 데이터로 커스텀 마커를 생성합니다.
                    martsData.forEach((mart) => {
                        // 1. react-icons SVG를 문자열로 변환
                        const iconString = ReactDOMServer.renderToString(
                            <TbShoppingCartFilled color="#FD79A8" size="32" />
                        );

                        // 2. SVG 문자열을 데이터 URL로 생성
                        const dataUrl = `data:image/svg+xml;charset=utf-8,${encodeURIComponent(iconString)}`;

                        // 3. 카카오맵에서 사용할 마커 이미지 객체 생성
                        const imageSize = new window.kakao.maps.Size(32, 32);
                        const markerImage = new window.kakao.maps.MarkerImage(dataUrl, imageSize);

                        // 4. 최종 마커 생성
                        new window.kakao.maps.Marker({
                            position: new window.kakao.maps.LatLng(mart.latitude, mart.longitude),
                            title: mart.martName,
                            image: markerImage,
                            map: map,
                        });
                    });

                    console.log(" 지도가 성공적으로 생성되었고 커스텀 마커를 표시했습니다.");
                    isMapInitialized.current = true;
                }
            });
        };

        initializeMap();
    }, []);

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